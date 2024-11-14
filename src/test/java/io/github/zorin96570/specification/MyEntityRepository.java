package io.github.zorin96570.specification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyEntityRepository extends JpaRepository<MyEntity, Long> {

    List<MyEntity> findAll(Specification<MyEntity> specification, Pageable pageable);
}
