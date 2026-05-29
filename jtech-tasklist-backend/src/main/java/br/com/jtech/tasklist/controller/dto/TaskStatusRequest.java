package br.com.jtech.tasklist.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TaskStatusRequest(boolean completed) implements Serializable {
}