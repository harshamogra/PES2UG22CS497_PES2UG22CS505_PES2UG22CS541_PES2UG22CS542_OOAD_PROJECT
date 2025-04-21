package project.OOAD_PROJECT.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import project.OOAD_PROJECT.model.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {}
