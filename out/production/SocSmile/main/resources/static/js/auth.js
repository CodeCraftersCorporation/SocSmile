const auth = {
    _getUser() {
        const userStr = localStorage.getItem('socsmile_user');
        return userStr ? JSON.parse(userStr) : null;
    },

    getSession() {
        const token = api._getToken();
        const user = this._getUser();
        return { token, user };
    },

    isLoggedIn() {
        return !!api._getToken() && !!this._getUser();
    },

    setSession(token, user) {
        localStorage.setItem('socsmile_token', token);
        // Remove password from user object
        const safeUser = { ...user };
        delete safeUser.password;
        localStorage.setItem('socsmile_user', JSON.stringify(safeUser));
    },

    clearSession() {
        localStorage.removeItem('socsmile_token');
        localStorage.removeItem('socsmile_user');
    },

    getCurrentUser() {
        return this._getUser();
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = 'login.html';
        }
    },

    logout() {
        api.logout().catch(() => {});
        this.clearSession();
        window.location.href = 'login.html';
    },

    updateNavbar() {
        const user = this._getUser();
        if (!user) return;

        const navAvatar = document.getElementById('navAvatar');
        const navUsername = document.getElementById('navUsername');

        if (navAvatar) navAvatar.src = user.avatar;
        if (navUsername) navUsername.textContent = user.displayName;
    }
};
