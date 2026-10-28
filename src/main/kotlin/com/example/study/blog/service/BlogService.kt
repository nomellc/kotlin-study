package com.example.study.blog.service

import com.example.study.blog.dto.BlogDto
import com.example.study.blog.entity.WordCount
import com.example.study.blog.repository.WordRepository
import com.example.study.core.exception.InvalidInputException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono

@Service
class BlogService(
    val wordRepository: WordRepository
) {
    @Value("\${REST_API_KEY}")
    lateinit var restApiKey: String

    fun searchKakao(blogDto: BlogDto) : String? {
        val msgList = mutableListOf<ExceptionMsg>()

        val webClient = WebClient
            .builder()
            .baseUrl("https://dapi.kakao.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()

        val response = webClient
            .get()
            .uri {it.path("/v2/search/blog")
                .queryParam("query", blogDto.query
                    ?: throw InvalidInputException("query parameter required"))
                .queryParam("sort", blogDto.sort
                    ?: throw InvalidInputException("sort parameter required"))
                .queryParam("page", blogDto.page
                    ?: throw InvalidInputException("page parameter required"))
                .queryParam("size", blogDto.size
                    ?: throw InvalidInputException("size parameter required"))
                .build()
            }.header("Authorization", "KakaoAK $restApiKey")
            .retrieve()
            .bodyToMono<String>()

        val result = response.block()

        val lowQuery: String = blogDto.query.lowercase()
        val word: WordCount = wordRepository.findById(lowQuery).orElse(WordCount(lowQuery))
        word.cnt++

        wordRepository.save(word)

        return result
    }

    fun searchWordRank(): List<WordCount> = wordRepository.findTopByOrderByCntDesc()
}

private enum class ExceptionMsg(val msg: String) {
    EMPTY_QUERY("query parameter required"),
    NOT_INT_SORT("sort parameter one of accuracy and recency"),
    LESS_THAN_MIN("page is less than min"),
    MORE_THAN_MAX("page is more than max")
}