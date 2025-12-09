package com.pilli3800.inventario.data.models.auditoria;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

public class RegistroAuditoriaListener {

    @PrePersist
    public void prePersist(RegistroAuditoria e){
        e.setFcCreacion(LocalDateTime.now());
        e.setUsuCreacion(currentUser());
    }

    @PreUpdate
    public void preUpdate(RegistroAuditoria e){
        e.setFcActualizacion(LocalDateTime.now());
        e.setUsuActualizacion(currentUser());
    }

    private String currentUser(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || auth.getName().equals("anonymousUser")){
            return "SYSTEM";
        }
        return auth.getName();
    }
}