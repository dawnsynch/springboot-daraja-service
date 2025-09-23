package com.dawnsynch.darajaapi.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultParameters {

    @JsonProperty("ResultParameter")
    private List<ResultParameterItem> resultParameter;
}
