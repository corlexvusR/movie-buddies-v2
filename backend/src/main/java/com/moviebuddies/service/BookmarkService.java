package com.moviebuddies.service;

import com.moviebuddies.dto.response.BookmarkResponse;
import com.moviebuddies.dto.response.MovieBookmarkStatsResponse;
import com.moviebuddies.entity.Bookmark;
import com.moviebuddies.entity.Movie;
import com.moviebuddies.entity.User;
import com.moviebuddies.exception.BusinessException;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.BookmarkRepository;
import com.moviebuddies.repository.MovieRepository;
import com.moviebuddies.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 북마크 관련 비즈니스 로직 서비스
 * 사용자의 영화 북마크 추가, 삭제, 조회 및 통계 기능 제공
 * 캐싱을 통한 성능 최적화 및 중복 북마크 방지 로직 포함
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 북마크 추가
     * 중복 북마크 체크 후 새로운 북마크를 생성하고 관련 캐시를 무효화
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 생성된 북마크 정보
     * @throws ResourceNotFoundException 사용자 또는 영화를 찾을 수 없는 경우
     * @throws BusinessException 이미 북마크된 영화인 경우
     */
    @Transactional
    @CacheEvict(value = {"userBookmarks", "movieDetail"}, allEntries = true)
    public BookmarkResponse addBookmark(Long userId, Long movieId) {

        log.info("북마크 추가 - 사용자 ID: {}, 영화 ID: {}", userId, movieId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("영화", movieId));

        // 중복 북마크 확인
        if (bookmarkRepository.existsByUserIdAndMovieId(userId, movieId)) {
            throw BusinessException.conflict("이미 북마크된 영화입니다.");
        }

        Bookmark bookmark = Bookmark.builder()
                .user(user)
                .movie(movie)
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(bookmark);
        log.info("북마크 추가 완료 - 북마크 ID: {}", savedBookmark.getId());

        return BookmarkResponse.from(savedBookmark);
    }

    /**
     * 기본 북마크 삭제
     * 북마크 존재 여부 확인 후 삭제하고 관련 캐시를 무효화
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @throws  ResourceNotFoundException 북마크를 찾을 수 없는 경우
     */
    @Transactional
    @CacheEvict(value = {"userBookmarks", "movieDetail"}, allEntries = true)
    public void removeBookmark(Long userId, Long movieId) {

        log.info("북마크 삭제 - 사용자 ID: {}, 영화 ID: {}", userId, movieId);

        Bookmark bookmark = bookmarkRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("북마크를 찾을 수 없습니다."));

        bookmarkRepository.delete(bookmark);
        log.info("북마크 삭제 완료 - 북마크 ID: {}", bookmark.getId());
    }

    /**
     * 사용자의 북마크 목록 조회 (페이징 지원)
     * 최신 북마크부터 정렬하여 반환하여 반환하며 경과를 캐싱
     *
     * @param username 사용자명
     * @param pageable 페이징 정보
     * @return 페이징된 북마크 목록
     * @throws ResourceNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Cacheable(value = "userBookmarks", key = "#username + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<BookmarkResponse> getUserBookmarks(String username, Pageable pageable) {

        log.info("사용자 북마크 목록 조회 - 사용자: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "username", username));

        Page<Bookmark> bookmarks = bookmarkRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return bookmarks.map(BookmarkResponse::from);
    }

    /**
     * 특정 영화의 북마크 여부 확인
     * 북마크 아이콘 표시나 버튼 상태 결정에 사용
     *
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 북마크 존재 여부
     */
    public boolean isBookmarked(Long userId, Long movieId) {
        return bookmarkRepository.existsByUserIdAndMovieId(userId, movieId);
    }

    /**
     * 사용자의 총 북마크 개수 조회
     * 사용자 프로필 통계나 대시보드에서 활용
     * 
     * @param userId 사용자 ID
     * @return 사용자의 총 북마크 개수
     */
    @Cacheable(value = "userBookmarkCount", key = "#userId")
    public long getUserBookmarkCount(Long userId) {
        return bookmarkRepository.countByUserId(userId);
    }

    /**
     * 특정 영화의 총 북마크 개수 조회
     * 영화 인기도 지표로 활용되며 영화 상세 페이지에 표시
     *
     * @param movieId 영화 ID
     * @return 해당 영화의 총 북마크 개수
     */
    @Cacheable(value = "movieBookmarkCount", key = "#movieId")
    public long getMovieBookmarkCount(Long movieId) {
        return bookmarkRepository.countByMovieId(movieId);
    }

    /**
     * 사용자가 북마크한 특정 장르의 영화 목록 조회
     * 장르별 북마크 필터링이나 맞춤형 추천에 활용
     *
     * @param userId 사용자 ID
     * @param genreId 장르 ID
     * @return 해당 장르의 북마크 목록
     */
    public List<BookmarkResponse> getBookmarksByGenre(Long userId, Long genreId) {
        log.info("장르별 북마크 조회 - 사용자 ID: {}, 장르 ID: {}", userId, genreId);

        List<Bookmark> bookmarks = bookmarkRepository.findByUserIdAndGenreId(userId, genreId);
        return bookmarks.stream()
                .map(BookmarkResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 가장 많이 북마크된 영화 순위 조회
     * 인기 영화 랭킹이나 추천 시스템에서 활용
     * Repository의 Object[] 결과를 DTO로 변환하여 타입 안정성 확보
     *
     * @param limit 조회할 영화 수
     * @return 북마크 수 기준 상위 영화 목록
     */
    @Cacheable(value = "mostBookmarkedMovies", key = "#limit")
    public List<MovieBookmarkStatsResponse> getMostBookmarkedMovies(int limit) {
        log.info("가장 많이 북마크된 영화 순위 조회 - 개수: {}", limit);

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = bookmarkRepository.findMostBookmarkedMovies(pageable);

        return results.stream()
                .map(result -> MovieBookmarkStatsResponse.builder()
                        .movieId((Long) result[0])
                        .movieTitle((String) result[1])
                        .bookmarkCount((Long) result[2])
                        .build())
                .collect(Collectors.toList());
    }
}
