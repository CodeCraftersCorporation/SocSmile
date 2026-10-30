const api = {
    _getToken() {
        return localStorage.getItem('socsmile_token');
    },

    _getHeaders() {
        const token = this._getToken();
        const headers = {
            'Content-Type': 'application/json'
        };
        if (token) {
            headers['X-Auth-Token'] = token;
        }
        return headers;
    },

    async _request(method, url, body) {
        const options = {
            method: method,
            headers: this._getHeaders()
        };
        if (body && (method === 'POST' || method === 'PUT')) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);
        const data = await response.json();

        if (!response.ok) {
            if (response.status === 401) {
                auth.logout();
                throw new Error('Session expired');
            }
            throw new Error(data.error || 'Request failed');
        }
        return data;
    },

    // Auth
    async register(username, password, displayName) {
        return this._request('POST', '/api/auth/register', { username, password, displayName });
    },

    async login(username, password) {
        return this._request('POST', '/api/auth/login', { username, password });
    },

    async logout() {
        return this._request('POST', '/api/auth/logout', {});
    },

    async getMe() {
        return this._request('GET', '/api/auth/me');
    },

    // Posts
    async getPosts() {
        return this._request('GET', '/api/posts');
    },

    async createPost(text, imageUrl) {
        return this._request('POST', '/api/posts', { text, imageUrl: imageUrl || '' });
    },

    // Likes
    async toggleLike(postId) {
        return this._request('POST', '/api/posts/like', { postId });
    },

    // Profile
    async getProfile(userId) {
        if (userId) {
            return this._request('GET', '/api/profile/' + userId);
        }
        return this._request('GET', '/api/profile/me');
    },

    // Generic
    async get(url) {
        return this._request('GET', url);
    },

    async post(url, body) {
        return this._request('POST', url, body);
    }
};
