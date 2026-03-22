package backend.academy.linktracker.scrapper.repository.sql.mapper;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Tag;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class TagSqlMapper implements RowMapper<Tag> {

    @Override
    public Tag mapRow(ResultSet rs, int rowNum) throws SQLException {
        Tag tag = new Tag(new Chat(rs.getLong("chat_id")), rs.getString("name"));
        tag.setId(rs.getLong("id"));
        return tag;
    }
}
