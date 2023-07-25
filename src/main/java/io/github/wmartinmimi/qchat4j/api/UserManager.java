package io.github.wmartinmimi.qchat4j.api;

import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/user")
public class UserManager {

    @GET
    @Path("/list")
    public List<String> list() {
        List<UserEntry> users = UserEntry.listAll();
        return users.stream().map(UserEntry::username).toList();
    }

    @GET
    @Path("/create/{username}/{password}")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String create(@PathParam("username") String username, @PathParam("password") String password) {
        if (UserEntry.isUniqueUsername(username)) {
            var entry = new UserEntry();
            entry.username = username;
            entry.passwordHash = BcryptUtil.bcryptHash(password);
            entry.persist();
            return "success";
        } else {
            return "username not unique";
        }
    }
}
