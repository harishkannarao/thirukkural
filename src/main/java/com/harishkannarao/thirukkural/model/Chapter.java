package com.harishkannarao.thirukkural.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public record Chapter(
        int number,
        String adikaramName,
        String adikaramTransliteration,
        String adikaramTranslation,
        String paulName,
        String paulTransliteration,
        String paulTranslation,
        String iyalName,
        String iyalTransliteration,
        String iyalTranslation,
        List<Couplet> kurals
) {

}
