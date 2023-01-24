package plub.plubserver.domain.todo.dto;

import lombok.Builder;
import org.springframework.data.domain.Page;
import plub.plubserver.common.dto.PageResponse;
import plub.plubserver.domain.todo.model.PlubbingTodo;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class PlubbingTodoDto {

    public record CreateTodoRequest(
            @NotBlank @Size(max = 15)
            String content,

            @NotBlank
            String date
    ) {
        @Builder
        public CreateTodoRequest {}

        public PlubbingTodo toEntity() {
            return PlubbingTodo.builder()
                    .content(content)
                    .date(date)
                    .isChecked(false)
                    .isProof(false)
                    .proofImage("")
                    .likes(0)
                    .build();
        }
    }

    public record TodoCardResponse(
            Long id,
            String content,
            String date,
            boolean isChecked,
            boolean isProof,
            String proofImage,
            int likes
    ) {
        @Builder
        public TodoCardResponse {}

        public static TodoCardResponse of(PlubbingTodo plubbingTodo) {
            return TodoCardResponse.builder()
                    .id(plubbingTodo.getId())
                    .content(plubbingTodo.getContent())
                    .date(plubbingTodo.getDate())
                    .isChecked(plubbingTodo.isChecked())
                    .isProof(plubbingTodo.isProof())
                    .proofImage(plubbingTodo.getProofImage())
                    .likes(plubbingTodo.getLikes())
                    .build();
        }
    }

    public record TodoListResponse(
            PageResponse<TodoCardResponse> todoList
    ){
        @Builder
        public TodoListResponse {}

        public static TodoListResponse of(Page<TodoCardResponse> todoCardPage) {
            return TodoListResponse.builder()
                    .todoList(PageResponse.of(todoCardPage))
                    .build();
        }

    }

    public record UpdateTodoRequest(
            @NotBlank @Size(max = 15)
            String content,

            @NotBlank
            String date
    ) {
    }

    public record TodoMessage(Object result) {
    }

    public record TodoIdResponse(Long todoId) {
    }

    public record ProofTodoRequest(
            String proofImage
    ) {
    }
}
