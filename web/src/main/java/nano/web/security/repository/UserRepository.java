package nano.web.security.repository;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nano.web.security.entity.NanoUser;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

import static nano.support.EntityUtils.slim;
import static nano.support.Sugar.*;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    @NonNull
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public void upsertUser(NanoUser nanoUser) {
        var sql = """
                INSERT INTO nano_user (id, username, firstname, is_bot, language_code)
                VALUES (:id, :username, :firstname, :isBot, :languageCode)
                ON CONFLICT (id)
                    DO UPDATE SET username      = EXCLUDED.username,
                                  firstname     = EXCLUDED.firstname,
                                  is_bot        = EXCLUDED.is_bot,
                                  language_code = EXCLUDED.language_code;
                """;
        var paramSource = new BeanPropertySqlParameterSource(nanoUser);
        this.jdbcTemplate.update(slim(sql), paramSource);
    }

    public NanoUser queryUserByToken(String token) {
        var sql = """
                SELECT nu.id            AS id,
                       nu.firstname     AS firstname,
                       nu.username      AS username,
                       nu.language_code AS language_code,
                       nu.is_bot        AS is_bot
                FROM nano_token nt
                         JOIN nano_user nu on nt.user_id = nu.id
                WHERE nt.status = 'VALID'
                  AND nt.token = :token;
                """;
        var rowMapper = new BeanPropertyRowMapper<>(NanoUser.class);
        var paramMap = Map.of("token", token);
        var userList = this.jdbcTemplate.query(slim(sql), paramMap, rowMapper);
        return getFirst(userList);
    }

}
