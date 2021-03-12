package com.ef.mediaroutingengine.services;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

@Component(value = "happyg")
public class UserAudtiting implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        //String uname = SecurityContextHolder.getContext().getAuthentication().getName();
        String uname = "ahmad";
        return Optional.of(uname);
    }

}