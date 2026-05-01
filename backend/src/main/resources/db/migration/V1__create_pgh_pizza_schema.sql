CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE contributor_applications (
    id UUID PRIMARY KEY,
    applicant_user_id UUID NOT NULL REFERENCES users(id),
    application_reason TEXT NOT NULL,
    status VARCHAR(40) NOT NULL,
    reviewed_by_user_id UUID REFERENCES users(id),
    reviewed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_contributor_applications_status ON contributor_applications(status);
CREATE INDEX idx_contributor_applications_created_at ON contributor_applications(created_at);

CREATE TABLE ratings (
    id UUID PRIMARY KEY,
    creator_user_id UUID NOT NULL REFERENCES users(id),
    restaurant_name VARCHAR(160) NOT NULL,
    sauce VARCHAR(120) NOT NULL,
    toppings VARCHAR(160) NOT NULL,
    crust VARCHAR(120) NOT NULL,
    overall_rating INTEGER NOT NULL CHECK (overall_rating BETWEEN 1 AND 10),
    comments TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_ratings_created_at ON ratings(created_at);
CREATE INDEX idx_ratings_creator_user_id ON ratings(creator_user_id);

CREATE TABLE blog_posts (
    id UUID PRIMARY KEY,
    author_user_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(180) NOT NULL,
    slug VARCHAR(180) NOT NULL UNIQUE,
    body TEXT NOT NULL,
    youtube_url VARCHAR(500),
    youtube_video_id VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_blog_posts_created_at ON blog_posts(created_at);
CREATE INDEX idx_blog_posts_author_user_id ON blog_posts(author_user_id);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
