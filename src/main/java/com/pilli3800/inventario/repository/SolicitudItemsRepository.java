package com.pilli3800.inventario.repository;

import com.pilli3800.inventario.data.models.enums.EstadoSolicitudItems;
import com.pilli3800.inventario.data.models.solicituditems.SolicitudItems;
import com.pilli3800.inventario.data.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SolicitudItemsRepository
        extends JpaRepository<SolicitudItems, Long>, JpaSpecificationExecutor<SolicitudItems> {

    boolean existsBySolicitanteAndEstadoIn(
            User solicitante,
            Collection<EstadoSolicitudItems> estados
    );
}

