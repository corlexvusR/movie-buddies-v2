import { Metadata } from "next";

interface SEOConfig {
  title?: string;
  description?: string;
  keywords?: string[];
  canonical?: string;
  ogImage?: string;
  ogImageAlt?: string;
  ogType?: "website" | "article" | "profile" | "video.movie";
  twitterCard?: "summary" | "summary_large_image";
  noindex?: boolean;
  nofollow?: boolean;
}

const DEFAULT_TITLE = "MovieBuddies - 영화 정보 및 소셜 플랫폼";
const DEFAULT_DESCRIPTION = "최신 영화 정보를 확인하고 친구들과 소통해보세요.";
const DEFAULT_KEYWORDS = [
  "영화",
  "리뷰",
  "추천",
  "소셜",
  "친구",
  "북마크",
  "평점",
];
const SITE_URL = process.env.NEXT_PUBLIC_SITE_URL || "https://moviebuddies.com";
const DEFAULT_OG_IMAGE = `${SITE_URL}/images/og-default.jpg`;

export function generateSEO({
  title,
  description = DEFAULT_DESCRIPTION,
  keywords = DEFAULT_KEYWORDS,
  canonical,
  ogImage = DEFAULT_OG_IMAGE,
  ogImageAlt,
  ogType = "website",
  twitterCard = "summary_large_image",
  noindex = false,
  nofollow = false,
}: SEOConfig): Metadata {
  const fullTitle = title ? `${title} | MovieBuddies` : DEFAULT_TITLE;
  const canonicalUrl = canonical ? `${SITE_URL}${canonical}` : undefined;

  const robots = [
    noindex ? "noindex" : "index",
    nofollow ? "nofollow" : "follow",
  ].join(", ");

  return {
    title: fullTitle,
    description,
    keywords: keywords.join(", "),
    robots,

    ...(canonicalUrl && {
      alternates: {
        canonical: canonicalUrl,
      },
    }),

    openGraph: {
      title: fullTitle,
      description,
      type: ogType,
      url: canonicalUrl,
      images: [
        {
          url: ogImage,
          alt: ogImageAlt || `${title} 이미지`,
        },
      ],
      siteName: "MovieBuddies",
      locale: "ko_KR",
    },

    twitter: {
      card: twitterCard,
      title: fullTitle,
      description,
      images: [ogImage],
    },
  };
}

// 영화 상세 페이지용 SEO 생성기
export function generateMovieSEO(movie: {
  id: number;
  title: string;
  overview?: string;
  releaseDate?: string;
  voteAverage: number;
  posterPath?: string;
  genres: Array<{ name: string }>;
}): Metadata {
  const title = `${movie.title} (${
    movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : ""
  })`;
  const description =
    movie.overview || `${movie.title}에 대한 정보와 리뷰를 확인해보세요.`;
  const keywords = [
    "영화",
    movie.title,
    ...movie.genres.map((g) => g.name),
    "리뷰",
    "평점",
  ];
  const canonical = `/movies/${movie.id}`;
  const ogImage = movie.posterPath
    ? `https://image.tmdb.org/t/p/w500${movie.posterPath}`
    : DEFAULT_OG_IMAGE;

  return generateSEO({
    title,
    description,
    keywords,
    canonical,
    ogImage,
    ogImageAlt: `${movie.title} 포스터`,
    ogType: "video.movie",
  });
}

// 사용자 프로필용 SEO 생성기
export function generateProfileSEO(user: {
  username: string;
  nickname: string;
  profileImage?: string;
}): Metadata {
  const title = `${user.nickname} (@${user.username})`;
  const description = `${user.nickname}님의 MovieBuddies 프로필을 확인해보세요.`;
  const canonical = `/profile/${user.username}`;
  const ogImage = user.profileImage || DEFAULT_OG_IMAGE;

  return generateSEO({
    title,
    description,
    canonical,
    ogImage,
    ogImageAlt: `${user.nickname}님의 프로필 이미지`,
    ogType: "profile",
  });
}
