package dev.moon5.web.repository;

import dev.moon5.web.domain.PrintQueue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrintQueueRepository extends JpaRepository<PrintQueue, Long> {

    List<PrintQueue> findByStatusOrderByRequestedAtAsc(String status);

    List<PrintQueue> findByLabelTemplateIdOrderByRequestedAtDesc(Long labelTemplateId);

}
