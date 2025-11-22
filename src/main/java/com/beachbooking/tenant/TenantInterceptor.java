package com.beachbooking.tenant;

import com.beachbooking.model.entity.Tenant;
import com.beachbooking.repository.TenantRepository;
import com.beachbooking.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * Interceptor che estrae il tenant da ogni richiesta HTTP.
 *
 * Strategie di identificazione tenant (in ordine di priorità):
 * 1. Dal JWT token (claim tenantId)
 * 2. Da header custom X-Tenant-ID
 * 3. Da subdomain (es: lido-napoli.beachbooking.com)
 * 4. Da parametro query ?tenantSlug=lido-napoli
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Skip per endpoint pubblici che non richiedono tenant
        String path = request.getRequestURI();
        if (isPublicEndpoint(path)) {
            return true;
        }

        UUID tenantId = null;

        // Strategia 1: Estrai da JWT token (priorità massima)
        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            tenantId = jwtTokenProvider.getTenantIdFromToken(jwt);
        }

        // Strategia 2: Da header custom X-Tenant-ID
        if (tenantId == null) {
            String tenantHeader = request.getHeader("X-Tenant-ID");
            if (StringUtils.hasText(tenantHeader)) {
                try {
                    tenantId = UUID.fromString(tenantHeader);
                } catch (IllegalArgumentException e) {
                    // Invalido UUID, ignora
                }
            }
        }

        // Strategia 3: Da subdomain
        if (tenantId == null) {
            String subdomain = extractSubdomain(request);
            if (StringUtils.hasText(subdomain)) {
                tenantId = findTenantIdBySlug(subdomain);
            }
        }

        // Strategia 4: Da query parameter
        if (tenantId == null) {
            String tenantSlug = request.getParameter("tenantSlug");
            if (StringUtils.hasText(tenantSlug)) {
                tenantId = findTenantIdBySlug(tenantSlug);
            }
        }

        // Imposta il tenant nel contesto
        if (tenantId != null) {
            TenantContext.setTenantId(tenantId);
        } else if (!isPublicEndpoint(path)) {
            // Se non troviamo il tenant e non è un endpoint pubblico, errore
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Tenant not specified or not found\"}");
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // IMPORTANTE: Pulisci il TenantContext per evitare memory leak
        TenantContext.clear();
    }

    /**
     * Estrae il JWT token dall'header Authorization.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Estrae il subdomain dalla richiesta.
     * Es: lido-napoli.beachbooking.com -> "lido-napoli"
     */
    private String extractSubdomain(HttpServletRequest request) {
        String host = request.getServerName();
        String[] parts = host.split("\\.");

        // Se abbiamo almeno 3 parti (subdomain.domain.tld), prendi la prima
        if (parts.length >= 3) {
            String subdomain = parts[0];
            // Ignora www e api
            if (!subdomain.equals("www") && !subdomain.equals("api")) {
                return subdomain;
            }
        }

        return null;
    }

    /**
     * Trova il tenant ID dal slug.
     */
    private UUID findTenantIdBySlug(String slug) {
        return tenantRepository.findBySlug(slug)
                .map(Tenant::getId)
                .orElse(null);
    }

    /**
     * Verifica se un endpoint è pubblico e non richiede tenant.
     */
    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/api/tenants/register") ||
                path.startsWith("/api/tenants/check-slug") ||
                path.equals("/health") ||
                path.startsWith("/actuator/");
    }
}