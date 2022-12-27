package plub.plubserver.domain.plubbing.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import plub.plubserver.domain.account.config.AccountCode;
import plub.plubserver.domain.account.exception.AccountException;
import plub.plubserver.domain.account.model.Account;
import plub.plubserver.domain.plubbing.config.PlubbingCode;
import plub.plubserver.domain.plubbing.dto.PlubbingDto.*;
import plub.plubserver.domain.plubbing.exception.PlubbingException;
import plub.plubserver.domain.plubbing.model.AccountPlubbing;
import plub.plubserver.domain.plubbing.model.AccountPlubbingStatus;
import plub.plubserver.domain.account.service.AccountService;
import plub.plubserver.domain.category.model.PlubbingSubCategory;
import plub.plubserver.domain.category.model.SubCategory;
import plub.plubserver.domain.category.service.CategoryService;
import plub.plubserver.domain.plubbing.model.Plubbing;
import plub.plubserver.domain.plubbing.model.PlubbingPlace;
import plub.plubserver.domain.plubbing.model.PlubbingStatus;
import plub.plubserver.domain.plubbing.repository.*;
import plub.plubserver.domain.recruit.model.RecruitQuestion;
import plub.plubserver.domain.recruit.model.Recruit;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlubbingService {
    private final PlubbingRepository plubbingRepository;
    private final CategoryService categoryService;
    private final AccountService accountService;
    private final AccountPlubbingRepository accountPlubbingRepository;

    private void createRecruit(CreatePlubbingRequest createPlubbingRequest, Plubbing plubbing) {
        // 모집 질문글 엔티티화
        List<RecruitQuestion> recruitQuestionList = createPlubbingRequest.questions().stream()
                .map(it -> RecruitQuestion.builder()
                        .questionTitle(it)
                        .build())
                .toList();

        // 모집 자동 생성
        Recruit recruit = Recruit.builder()
                .title(createPlubbingRequest.title())
                .introduce(createPlubbingRequest.introduce())
                .plubbing(plubbing)
                .recruitQuestionList(recruitQuestionList)
                .questionNum(recruitQuestionList.size())
                .build();

        // 질문 - 모집 매핑
        recruitQuestionList.forEach(it -> it.addRecruit(recruit));

        // 모임 - 모집 매핑
        plubbing.addRecruit(recruit);
    }

    private void connectSubCategories(CreatePlubbingRequest createPlubbingRequest, Plubbing plubbing) {
        // 서브 카테고리 가져오기
        List<SubCategory> subCategories = createPlubbingRequest.subCategoryIds()
                .stream()
                .map(categoryService::getSubCategory)
                .toList();

        // 서브 카테고리 - 모임 매핑 (plubbingSubCategory 엔티티 생성)
        List<PlubbingSubCategory> plubbingSubCategories = subCategories.stream()
                .map(subCategory -> PlubbingSubCategory.builder()
                        .subCategory(subCategory)
                        .plubbing(plubbing)
                        .build())
                .toList();

        // 플러빙 객체에 추가 - 더티체킹으로 자동 엔티티 저장
        plubbing.addPlubbingSubCategories(plubbingSubCategories);
    }


    @Transactional
    public Long createPlubbing(CreatePlubbingRequest createPlubbingRequest) {
        // 모임 생성자(호스트) 가져오기
        Account owner = accountService.getCurrentAccount();

        // Plubbing 엔티티 생성 및 저장
        Plubbing plubbing = plubbingRepository.save(createPlubbingRequest.toEntity());

        // days 매핑
        plubbing.addPlubbingMeetingDay(createPlubbingRequest.getPlubbingMeetingDay(plubbing));

        // 오프라인이면 장소도 저장 (온라인 이면 기본값 저장)
        switch (plubbing.getOnOff().name()) {
            case "OFF" -> plubbing.addPlubbingPlace(PlubbingPlace.builder()
                    .address(createPlubbingRequest.address())
                    .placePositionX(createPlubbingRequest.placePositionX())
                    .placePositionY(createPlubbingRequest.placePositionY())
                    .build());

            case "ON" -> plubbing.addPlubbingPlace(new PlubbingPlace());
        }

        // Plubbing - PlubbingSubCategory 매핑
        connectSubCategories(createPlubbingRequest, plubbing);

        // Plubbing - AccountPlubbing 매핑
        plubbing.addAccountPlubbing(AccountPlubbing.builder()
                .isHost(true)
                .account(owner)
                .plubbing(plubbing)
                .accountPlubbingStatus(AccountPlubbingStatus.ACTIVE)
                .build()
        );

        // 모집 자동 생성 및 매핑
        createRecruit(createPlubbingRequest, plubbing);

        plubbingRepository.flush(); // flush를 안 하면 recruitId가 null로 들어감

        return plubbing.getId();
    }

    public List<MyPlubbingResponse> getMyPlubbing(Boolean isHost) {
        Account currentAccount = accountService.getCurrentAccount();

        return accountPlubbingRepository.findAllByAccountAndIsHostAndAccountPlubbingStatus(currentAccount, isHost, AccountPlubbingStatus.ACTIVE)
                .orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING)).stream()
                .map(MyPlubbingResponse::of).collect(Collectors.toList());
    }

    public MainPlubbingResponse getMainPlubbing(Long plubbingId) {
        Account currentAccount = accountService.getCurrentAccount();
        if (!accountPlubbingRepository.existsByAccountAndPlubbingId(currentAccount, plubbingId))
            throw new PlubbingException(PlubbingCode.FORBIDDEN_ACCESS_PLUBBING);

        Plubbing plubbing = plubbingRepository.findById(plubbingId).orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING));
        checkPlubbingStatus(plubbing);

        List<Account> accounts = accountPlubbingRepository.findAllByPlubbingId(plubbingId)
                .orElseThrow(() -> new AccountException(AccountCode.NOT_FOUND_ACCOUNT)).stream()
                .map(AccountPlubbing::getAccount).collect(Collectors.toList());

        return MainPlubbingResponse.of(plubbing, accounts);
    }

    @Transactional
    public PlubbingMessage deletePlubbing(Long plubbingId) {
        Plubbing plubbing = plubbingRepository.findById(plubbingId).orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING));
        checkPlubbingStatus(plubbing);
        checkAuthority(plubbing);

        plubbing.deletePlubbing();

        accountPlubbingRepository.findAllByPlubbingId(plubbingId)
                .orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING))
                .forEach(a -> a.changeStatus(AccountPlubbingStatus.END));

        return new PlubbingMessage(true);
    }

    @Transactional
    public PlubbingMessage endPlubbing(Long plubbingId) {
        Plubbing plubbing = plubbingRepository.findById(plubbingId).orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING));
        checkPlubbingStatus(plubbing);
        checkAuthority(plubbing);
        List<AccountPlubbing> accountPlubbingList = accountPlubbingRepository.findAllByPlubbingId(plubbingId)
                .orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING));
        if (plubbing.getStatus().equals(PlubbingStatus.END)) {
            plubbing.endPlubbing(PlubbingStatus.ACTIVE);
            accountPlubbingList.forEach(a -> a.changeStatus(AccountPlubbingStatus.ACTIVE));
        } else if (plubbing.getStatus().equals(PlubbingStatus.ACTIVE)) {
            plubbing.endPlubbing(PlubbingStatus.END);
            accountPlubbingList.forEach(a -> a.changeStatus(AccountPlubbingStatus.END));
        }
        return new PlubbingMessage(true);
    }

    @Transactional
    public PlubbingResponse updatePlubbing(Long plubbingId, UpdatePlubbingRequest updatePlubbingRequest) {
        Plubbing plubbing = plubbingRepository.findById(plubbingId).orElseThrow(() -> new PlubbingException(PlubbingCode.NOT_FOUND_PLUBBING));
        checkPlubbingStatus(plubbing);
        checkAuthority(plubbing);
        plubbing.updatePlubbing(updatePlubbingRequest.name(), updatePlubbingRequest.goal(), updatePlubbingRequest.mainImageUrl());
        return PlubbingResponse.of(plubbing);
    }

    private void checkAuthority(Plubbing plubbing) {
        Account currentAccount = accountService.getCurrentAccount();
        AccountPlubbing accountPlubbing = accountPlubbingRepository.findByAccountAndPlubbing(currentAccount, plubbing).orElseThrow(() -> new AccountException(AccountCode.NOT_FOUND_ACCOUNT));
        if (!accountPlubbing.isHost()) throw new PlubbingException(PlubbingCode.NOT_HOST);
    }

    private void checkPlubbingStatus(Plubbing plubbing) {
        if (plubbing.getStatus().equals(PlubbingStatus.END) || !plubbing.isVisibility())
            throw new PlubbingException(PlubbingCode.DELETED_STATUS_PLUBBING);
    }

}
