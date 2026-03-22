package backend.academy.linktracker.scrapper.repository.sql.mapper;

import backend.academy.linktracker.scrapper.model.Chat;
import backend.academy.linktracker.scrapper.model.Filter;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class FilterSqlMapper implements RowMapper<Filter> {

    @Override
    public Filter mapRow(ResultSet rs, int rowNum) throws SQLException {
        Filter filter = new Filter(new Chat(rs.getLong("chat_id")), rs.getString("value"));
        filter.setId(rs.getLong("id"));
        return filter;
    }
}
