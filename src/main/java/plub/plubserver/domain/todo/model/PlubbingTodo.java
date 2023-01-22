package plub.plubserver.domain.todo.model;

import plub.plubserver.domain.timeline.model.PlubbingTimeline;

import javax.persistence.*;

@Entity
public class PlubbingTodo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long id;

    private String title;
    private String todoImg;
    private String content;
    private int likes;

    // 투두(다) - 타임라인(1)
    @ManyToOne
    @JoinColumn(name = "timeline_id")
    private PlubbingTimeline timeLine;
}
