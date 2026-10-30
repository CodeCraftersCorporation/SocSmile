const feed = {
    init() {
        auth.updateNavbar();
        this.setupPostCreator();
        this.loadPosts();
    },

    setupPostCreator() {
        const textarea = document.getElementById('postText');
        const charCount = document.getElementById('charCount');
        const postBtn = document.getElementById('postBtn');

        if (textarea) {
            textarea.addEventListener('input', () => {
                const len = textarea.value.length;
                charCount.textContent = len + '/500';
                if (len > 500) {
                    charCount.style.color = '#e74c3c';
                } else {
                    charCount.style.color = '';
                }
            });
        }

        if (postBtn) {
            postBtn.addEventListener('click', () => this.createPost());
            textarea.addEventListener('keydown', (e) => {
                if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
                    this.createPost();
                }
            });
        }
    },

    async createPost() {
        const textarea = document.getElementById('postText');
        const text = textarea.value.trim();

        if (!text) return;

        try {
            const user = auth.getCurrentUser();
            const post = await api.createPost(text);

            // Update creator avatar
            const creatorAvatar = document.getElementById('creatorAvatar');
            if (creatorAvatar) creatorAvatar.src = user.avatar;

            textarea.value = '';
            document.getElementById('charCount').textContent = '0/500';
            document.getElementById('charCount').style.color = '';

            // Reload posts
            await this.loadPosts();
        } catch (err) {
            console.error('Create post error:', err);
            alert('Ошибка создания поста: ' + err.message);
        }
    },

    async loadPosts() {
        const feedEl = document.getElementById('feed');
        feedEl.innerHTML = '<div class="feed-loading"><div class="spinner"></div><p>Загрузка...</p></div>';

        try {
            const posts = await api.getPosts();
            this.renderPosts(posts);
            this.updateStats(posts);
        } catch (err) {
            feedEl.innerHTML = '<div class="empty-state"><div class="icon">😕</div><h3>Ошибка загрузки ленты</h3><p>' + escapeHtml(err.message) + '</p></div>';
        }
    },

    renderPosts(posts) {
        const feedEl = document.getElementById('feed');

        if (posts.length === 0) {
            feedEl.innerHTML = '<div class="empty-state"><div class="icon">📝</div><h3>Пока нет постов</h3><p>Будьте первым — поделитесь чем-нибудь!</p></div>';
            return;
        }

        feedEl.innerHTML = posts.map(post => this.renderPostCard(post)).join('');

        // Setup like buttons
        feedEl.querySelectorAll('.like-btn').forEach(btn => {
            btn.addEventListener('click', () => this.toggleLike(btn));
        });

        // Setup avatar clicks
        feedEl.querySelectorAll('.post-avatar, .post-author').forEach(el => {
            el.addEventListener('click', () => {
                const userId = el.closest('.post-card')?.dataset?.authorId;
                if (userId) {
                    window.location.href = 'profile.html?id=' + userId;
                }
            });
        });
    },

    renderPostCard(post) {
        const likedClass = post.liked ? 'liked' : '';
        const heartIcon = post.liked ? '❤️' : '🤍';
        const imageHtml = post.imageUrl
            ? `<img src="${escapeHtml(post.imageUrl)}" class="post-image" alt="">`
            : '';

        return `
            <div class="card post-card" data-author-id="${post.authorId}">
                <div class="post-header">
                    <img class="post-avatar" src="${escapeHtml(post.authorAvatar)}" alt="">
                    <div>
                        <div class="post-author">${escapeHtml(post.authorName)}</div>
                        <div class="post-time">${escapeHtml(post.createdAt)}</div>
                    </div>
                </div>
                <div class="post-text">${escapeHtml(post.text)}</div>
                ${imageHtml}
                <div class="post-actions">
                    <button class="post-action-btn like-btn ${likedClass}" data-post-id="${post.id}">
                        <span class="heart-icon">${heartIcon}</span>
                        <span>${post.likeCount || 0}</span>
                    </button>
                </div>
            </div>
        `;
    },

    async toggleLike(btn) {
        const postId = btn.dataset.postId;
        const heartIcon = btn.querySelector('.heart-icon');
        const countSpan = btn.querySelector('span:last-child');

        // Optimistic update
        const isLiked = btn.classList.contains('liked');
        btn.classList.toggle('liked');
        heartIcon.textContent = isLiked ? '🤍' : '❤️';
        countSpan.textContent = (parseInt(countSpan.textContent) || 0) + (isLiked ? -1 : 1);

        try {
            const data = await api.toggleLike(postId);
            // Update with server response
            btn.dataset.postId = data.id;
            const newLiked = data.liked;
            btn.className = 'post-action-btn like-btn' + (newLiked ? ' liked' : '');
            heartIcon.textContent = newLiked ? '❤️' : '🤍';
            countSpan.textContent = data.likeCount || 0;
        } catch (err) {
            // Revert on error
            btn.classList.toggle('liked');
            heartIcon.textContent = isLiked ? '❤️' : '🤍';
            countSpan.textContent = (parseInt(countSpan.textContent) || 0) + (isLiked ? 1 : -1);
            console.error('Like error:', err);
        }
    },

    updateStats(posts) {
        const postCount = document.getElementById('postCount');
        const likeCount = document.getElementById('likeCount');
        const userCount = document.getElementById('userCount');

        if (postCount) postCount.textContent = posts.length;

        let totalLikes = 0;
        posts.forEach(p => totalLikes += (p.likeCount || 0));
        if (likeCount) likeCount.textContent = totalLikes;
    }
};

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
