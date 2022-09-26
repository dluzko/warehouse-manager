package com.luzko.warehouse.repository;

import com.luzko.warehouse.model.Blocking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockingRepository extends JpaRepository<Blocking, Integer> {}
