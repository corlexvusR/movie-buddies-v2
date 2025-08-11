package com.moviebuddies.service;

import com.moviebuddies.dto.request.ReviewRequest;
import com.moviebuddies.dto.response.MovieRatingDistributionResponse;
import com.moviebuddies.dto.response.ReviewResponse;
import com.moviebuddies.entity.Movie;
import com.moviebuddies.entity.Review;
import com.moviebuddies.entity.User;
import com.moviebuddies.exception.BusinessException;
import com.moviebuddies.exception.ResourceNotFoundException;
import com.moviebuddies.repository.MovieRepository;
import com.moviebuddies.repository.ReviewRepository;
import com.moviebuddies.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 리뷰 비즈니스 로직 서비스
 * 영화 리뷰의 CRUD 및 조회 기능 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;

    /**
     * 특정 영화의 모든 리뷰 조회 (영화 상세 페이지용)
     *
     * @param movieId 영화 ID
     * @param pageable 페이징 정보
     * @return 영화의 리뷰 목록 (페이징)
     */
    @Cacheable(value = "movieReviews", key = "#movieId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ReviewResponse> getReviewsByMovie(Long movieId, Pageable pageable) {
        log.info("영화 리뷰 조회 - 영화 ID: {}", movieId);

        movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("영화", movieId));

        Page<Review> reviews = reviewRepository.findByMovieIdOrderByCreatedAtDesc(movieId, pageable);
        return reviews.map(ReviewResponse::from);
    }

    /**
     * 특정 사용자의 모든 리뷰 조회 (마이페이지용)
     * 
     * @param username 사용자명
     * @param pageable 페이징 정보
     * @return 사용자의 리뷰 목록 (페이징)
     */
    @Cacheable(value = "userReviews", key = "#username + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ReviewResponse> getReviewsByUser(String username, Pageable pageable) {
        log.info("사용자 리뷰 조회 - 사용자: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", "username", username));

        Page<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable);
        return reviews.map(ReviewResponse::from);
    }

    /**
     * 새 리뷰 작성
     *
     * @param userId 작성자 ID
     * @param request 리뷰 작성 요청 데이터
     * @return 작성된 리뷰 정보
     * @throws BusinessException 중복 리뷰 또는 유효하지 않은 평점
     */
    @Transactional
    @CacheEvict(value = {"movieReviews", "userReviews", "movieDetail"}, allEntries = true)
    public ReviewResponse createReview(Long userId, ReviewRequest request) {
        log.info("리뷰 작성 - 사용자 ID: {}, 영화 ID: {}", userId, request.getMovieId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자", userId));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("영화", request.getMovieId()));

        // 중복 리뷰 확인 (한 사용자는 하나의 영화에 하나의 리뷰만 작성 가능)
        if (reviewRepository.existsByUserIdAndMovieId(userId, request.getMovieId())) {
            throw BusinessException.conflict("이미 이 영화에 대한 리뷰를 작성하셨습니다.");
        }
        
        // 평점 유효성 검사 (1 - 5 점)
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw BusinessException.conflict("평점은 1에서 5 사이여야 합니다.");
        }

        Review review = Review.builder()
                .user(user)
                .movie(movie)
                .content(request.getContent())
                .rating(request.getRating())
                .build();

        Review savedReview = reviewRepository.save(review);
        log.info("리뷰 작성 완료 - 리뷰 ID: {}", savedReview.getId());

        return ReviewResponse.from(savedReview);
    }

    /**
     * 작성된 리뷰 수정
     *
     * @param userId 작성자 ID
     * @param movieId 영화 ID
     * @param request 리뷰 수정 요청 데이터
     * @return 수정된 리뷰 정보
     * @throws BusinessException 유효하지 않은 평점
     */
    @Transactional
    @CacheEvict(value = {"movieReviews", "userReviews", "movieDetail"}, allEntries = true)
    public ReviewResponse updateReview(Long userId, Long movieId, ReviewRequest request) {
        log.info("리뷰 수정 - 사용자 ID: {}, 영화 ID: {}", userId, movieId);

        Review review = reviewRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다."));

        // 평점 유효성 검사 (1 - 5 점)
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw BusinessException.conflict("평점은 1에서 5 사이여야 합니다.");
        }

        review.updateReview(request.getContent(), request.getRating());

        Review updatedReview = reviewRepository.save(review);
        log.info("리뷰 수정 완료 - 리뷰 ID: {}", updatedReview.getId());

        return ReviewResponse.from(updatedReview);
    }

    /**
     * 리뷰 삭제
     *
     * @param userId 작성자 ID
     * @param movieId 영화 ID
     * @param reviewId 리뷰 ID
     * @throws BusinessException 권한이 없거나 영화 ID가 불일치하는 경우
     */
    @Transactional
    @CacheEvict(value = {"movieReviews", "userReviews", "movieDetail"}, allEntries = true)
    public void deleteReview(Long userId, Long movieId, Long reviewId) {
        log.info("리뷰 삭제 - 사용자 ID: {}, 영화 ID: {}, 리뷰 ID: {}", userId, movieId, reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰", reviewId));

        // 권한 확인 (자신이 작성한 리뷰만 삭제 가능)
        if (!review.getUser().getId().equals(userId)) {
            throw BusinessException.forbidden("자신이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        // 영화 ID 일치 확인
        if (!review.getMovie().getId().equals(movieId)) {
            throw BusinessException.badRequest("영화 ID가 일치하지 않습니다.");
        }

        reviewRepository.delete(review);
        log.info("리뷰 삭제 완료 - 리뷰 ID: {}", reviewId);
    }

    /**
     * 사용자의 특정 영화에 대한 리뷰 조회 (영화 상세 페이지의 리뷰 섹션 상단에 자신이 쓴 리뷰가 위치)
     * @param userId 사용자 ID
     * @param movieId 영화 ID
     * @return 사용자의 해당 영화 리뷰
     */
    public ReviewResponse getUserReviewForMovie(Long userId, Long movieId) {
        log.info("사용자 영화 리뷰 조회 - 사용자 ID: {}, 영화 ID: {}", userId, movieId);

        Review review = reviewRepository.findByUserIdAndMovieId(userId, movieId)
                .orElseThrow(() -> new ResourceNotFoundException("리뷰를 찾을 수 없습니다."));

        return ReviewResponse.from(review);
    }

    /**
     * 영화 평점 분포 조회 (리뷰 섹션 상단의 시각화 용도)
     * @param movieId 영화 ID
     * @return 평점별 리뷰 수 분포 데이터
     */
    @Cacheable(value = "movieRatingDistribution", key = "#movieId")
    public MovieRatingDistributionResponse getMovieRatingDistribution(Long movieId) {
        log.info("영화 평점 분포 조회 - 영화 ID: {}", movieId);
        
        // 영화 존재 확인
        movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("영화", movieId));

        List<Object[]> distribution = reviewRepository.getRatingDistributionByMovie(movieId);

        return MovieRatingDistributionResponse.builder()
                .movieId(movieId)
                .ratingDistribution(distribution)
                .build();
    }
}
