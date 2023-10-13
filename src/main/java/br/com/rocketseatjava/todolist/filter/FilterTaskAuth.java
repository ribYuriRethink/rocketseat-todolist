package br.com.rocketseatjava.todolist.filter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.rocketseatjava.todolist.user.UserModel;
import br.com.rocketseatjava.todolist.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public FilterTaskAuth(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if (!servletPath.startsWith("/tasks")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        String authEncoded = authorization.substring("Basic".length()).trim();

        byte[] authDecoded = Base64.getDecoder().decode(authEncoded);
        String authString = new String(authDecoded);
        String[] credentials = authString.split(":");
        String username = credentials[0];
        String password = credentials[1];

        UserModel foundUser = userRepository.findByUsername(username);
        if (foundUser != null) {
            BCrypt.Result verifiedPassword = BCrypt.verifyer().verify(password.toCharArray(), foundUser.getPassword());
            if (verifiedPassword.verified) {
                request.setAttribute("idUser", foundUser.getId());
                filterChain.doFilter(request, response);
            } else {
                response.sendError(401);
            }
        } else {
            response.sendError(401);
        }
    }
}
