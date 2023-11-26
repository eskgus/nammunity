package com.eskgus.nammunity.util;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeywordUtil {
    @Getter
    @RequiredArgsConstructor
    public static class KeywordLists {
        private final List<String> includeKeywordList;
        private final List<String> excludeKeywordList;
    }

    public static <T> List<T> searchByField(EntityManager entityManager,
                                            EntityPathBase<T> queryType, StringExpression field, String keywords) {
        // keywords를 includeKeywordList(검색어)와 excludeKeywordList(검색 제외 단어)로 분리
        KeywordLists keywordLists = extractKeywords(keywords);
        List<String> includeKeywordList = keywordLists.getIncludeKeywordList();
        List<String> excludeKeywordList = keywordLists.getExcludeKeywordList();

        // main query 생성: includeKeywords 중 하나라도 포함돼있으면 추출
        BooleanBuilder builder = new BooleanBuilder();
        for (String includeKeyword : includeKeywordList) {
            builder.or(field.containsIgnoreCase(includeKeyword));
        }

        // subquery 생성: excludeKeywords 중 하나라도 포함돼있으면 추출 x
        BooleanBuilder builderSub = new BooleanBuilder();
        for (String excludeKeyword : excludeKeywordList) {
            builderSub.andNot(field.containsIgnoreCase(excludeKeyword));
        }

        // orderBy() 정렬 기준: 각 쿼리 타입(Q 클래스)의 id 필드
        NumberPath<Long> id = Expressions.numberPath(Long.class, queryType, "id");

        // 쿼리 생성 + 실행
        JPAQueryFactory query = new JPAQueryFactory(entityManager);
        return query.selectFrom(queryType).where(builder.and(queryType.in(
                JPAExpressions.selectFrom(queryType).where(builderSub))))
                .orderBy(id.desc()).fetch();
    }

    public static KeywordLists extractKeywords(String keywords) {
        int length = keywords.length();
        int excludeIndex = (keywords.indexOf(" -") == -1) ? length : keywords.indexOf(" -");

        // 검색어에 " -"가 있으면 해당 단어는 검색 결과에서 제외 + 제외 단어는 ","로 구분
        List<String> excludeKeywordList = new ArrayList<>();
        if (excludeIndex != length) {
            excludeKeywordList = Arrays.asList(keywords.substring(excludeIndex + 2).split(","));
        }

        // 검색 제외 단어를 뺀 keywords는 띄어쓰기로 나누기
        List<String> includeKeywordList = Arrays.asList(keywords.substring(0, excludeIndex).split("\\s+"));

        return new KeywordLists(includeKeywordList, excludeKeywordList);
    }
}
