package plub.plubserver.domain.todo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import plub.plubserver.domain.account.model.Account;
import plub.plubserver.domain.todo.model.TodoLike;
import plub.plubserver.domain.todo.model.TodoTimeline;

public interface TodoLikeRepository extends JpaRepository<TodoLike, Long> {
    boolean existsByAccountAndTodoTimeline(Account account, TodoTimeline todoTimeline);
    void deleteByAccountAndTodoTimeline(Account account, TodoTimeline todoTimeline);
}
