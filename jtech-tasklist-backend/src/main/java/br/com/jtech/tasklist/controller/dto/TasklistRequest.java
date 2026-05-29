/*
 *  @(#)TasklistRequest.java
 *
 *  Copyright (c) J-Tech Solucoes em Informatica.
 *  All Rights Reserved.
 *
 *  This software is the confidential and proprietary information of J-Tech.
 *  ("Confidential Information"). You shall not disclose such Confidential
 *  Information and shall use it only in accordance with the terms of the
 *  license agreement you entered into with J-Tech.
 *
 */
package br.com.jtech.tasklist.controller.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
* class TasklistRequest 
* 
* user angelo.vicente 
*/
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TasklistRequest(
        @NotBlank(message = "Task list name is required")
        @Size(max = 80, message = "Task list name must have at most 80 characters")
        String name
) implements Serializable {
}