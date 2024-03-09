package com.eskgus.nammunity.helper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class KeywordHelper {
    private List<String> includeKeywordList;
    private List<String> excludeKeywordList;

    public KeywordHelper extractKeywords(String keywords) {
        int length = keywords.length();
        int excludeIndex = (keywords.indexOf(" -") == -1) ? length : keywords.indexOf(" -");

        // 검색어에 " -"가 있으면 해당 단어는 검색 결과에서 제외 + 제외 단어는 ","로 구분
        if (excludeIndex != length) {
            this.excludeKeywordList = Arrays.asList(keywords.substring(excludeIndex + 2).split(","));
        } else {
            this.excludeKeywordList = new ArrayList<>();
        }

        // 검색 제외 단어를 뺀 keywords는 띄어쓰기로 나누기
        this.includeKeywordList = Arrays.asList(keywords.substring(0, excludeIndex).split("\\s+"));

        return new KeywordHelper(includeKeywordList, excludeKeywordList);
    }
}
