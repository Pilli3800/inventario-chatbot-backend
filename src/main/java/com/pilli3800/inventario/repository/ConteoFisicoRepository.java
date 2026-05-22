package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.conteofisico.ConteoFisico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ConteoFisicoRepository
        extends JpaRepository<ConteoFisico, Long>, JpaSpecificationExecutor<ConteoFisico> {
}
