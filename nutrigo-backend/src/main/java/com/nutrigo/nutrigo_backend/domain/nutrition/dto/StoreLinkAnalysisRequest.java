package com.nutrigo.nutrigo_backend.domain.nutrition.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreLinkAnalysisRequest {

    @NotBlank
    @JsonProperty("store_url")
    private String storeUrl;

    @Valid
    @NotNull
    @JsonProperty("user_info")
    private UserInfoRequest userInfo;
}
