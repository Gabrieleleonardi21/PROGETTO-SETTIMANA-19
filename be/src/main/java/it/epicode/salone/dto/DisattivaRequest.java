package it.epicode.salone.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Token del link nella mail (43 caratteri base64url)
public record DisattivaRequest(@NotBlank @Size(max = 100) String token) {
}
