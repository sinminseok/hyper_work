package hyper.run.auth.constants;


public final class AuthUrlPatterns {

    private static final String V1_BASE_API_URL = "/v1/api";

    public static final String[] GET_AUTH_WHITELIST = {
            V1_BASE_API_URL + "/users",
            V1_BASE_API_URL + "/users/email-exists",
            V1_BASE_API_URL + "/users/id",
    };

    public static final String[] POST_AUTH_WHITELIST = {
            V1_BASE_API_URL + "/google",
            V1_BASE_API_URL + "/auth/**",
            V1_BASE_API_URL + "/apple",
            V1_BASE_API_URL + "/users",
            V1_BASE_API_URL + "/users/password",
            V1_BASE_API_URL + "/games/test",
    };

    public static final String[] NOT_AUTH_URL = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/paypal/success",
            "/paypal/cancel",
            "/admin",
            "/admin/",
            "/admin",
            "/admin/**"
    };
}
