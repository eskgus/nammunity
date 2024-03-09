package com.eskgus.nammunity.helper;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;

import lombok.Builder;

import java.util.List;

public class SearchQueries<T, U> {    // T: listDto, U: entity
    private EssentialQuery<T, U> essentialQuery;
    private String keywords;
    private StringPath[] fields;

    @Builder
    public SearchQueries(EssentialQuery<T, U> essentialQuery,
                         String keywords, StringPath... fields) {
        this.essentialQuery = essentialQuery;
        this.keywords = keywords;
        this.fields = fields;
    }

    public JPAQuery<T> createQueryForSearchContents() {
        KeywordHelper keywordHelper = new KeywordHelper().extractKeywords(keywords);
        List<String> includeKeywordList = keywordHelper.getIncludeKeywordList();
        List<String> excludeKeywordList = keywordHelper.getExcludeKeywordList();

        BooleanBuilder mainWhereCondition = createMainWhereCondition(includeKeywordList);
        BooleanBuilder subWhereCondition = createSubWhereCondition(excludeKeywordList);

        return createQueryWithConditions(mainWhereCondition, subWhereCondition);
    }

    private BooleanBuilder createMainWhereCondition(List<String> includeKeywordList) {
        BooleanBuilder builder1 = new BooleanBuilder();
        for (String includeKeyword : includeKeywordList) {
            BooleanBuilder builder2 = new BooleanBuilder();
            for (StringPath field : fields) {
                builder2.or(field.containsIgnoreCase(includeKeyword));
            }
            builder1.or(builder2);
        }
        return builder1;
    }

    private BooleanBuilder createSubWhereCondition(List<String> excludeKeywordList) {
        BooleanBuilder builder1 = new BooleanBuilder();
        for (String excludeKeyword : excludeKeywordList) {
            BooleanBuilder builder2 = new BooleanBuilder();
            for (StringPath field : fields) {
                builder1.and(field.containsIgnoreCase(excludeKeyword).not());
            }
            builder1.and(builder2);
        }
        return builder1;
    }

    private JPAQuery<T> createQueryWithConditions(BooleanBuilder mainWhereCondition, BooleanBuilder subWhereCondition) {
        JPAQuery<T> query = essentialQuery.createBaseQuery(fields);
        return query.where(mainWhereCondition.and(essentialQuery.getQueryType().in(
                JPAExpressions.selectFrom(essentialQuery.getQueryType())
                        .where(subWhereCondition))));
    }
}
