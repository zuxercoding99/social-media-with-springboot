const API = CONFIG.API_BASE_URL;

/* ================= LOGOUT FORZADO ================= */
export async function forceLogout() {
  try {
    await fetch(API + "/api/v1/auth/logout", {
      method: "POST",
      credentials: "include",
    });
  } catch {
    // aunque falle, seguimos
  } finally {
    localStorage.clear();
    window.location.href = "index.html";
  }
}

/* ================= REFRESH TOKEN ================= */
let refreshPromise = null;
export async function tryRefreshToken() {
  if (refreshPromise) {
    return refreshPromise; // 👈 esperar refresh en curso
  }

  refreshPromise = (async () => {
    try {
      const res = await fetch(API + "/api/v1/auth/refresh", {
        method: "POST",
        credentials: "include",
      });

      if (!res.ok) return false;

      const data = await res.json();
      localStorage.setItem("accessToken", data.accessToken);
      return true;
    } catch {
      return false;
    } finally {
      refreshPromise = null;
    }
  })();

  return refreshPromise;
}

/* ================= FETCH CON AUTH ================= */
export async function fetchWithAuth(url, options = {}) {
  const accessToken = localStorage.getItem("accessToken");

  options.headers = {
    ...(options.headers || {}),
    Authorization: accessToken ? "Bearer " + accessToken : undefined,
  };

  let res = await fetch(url, options);

  // 401 → token expirado o usuario inexistente
  if (res.status === 401) {
    const refreshed = await tryRefreshToken();

    if (refreshed) {
      options.headers.Authorization =
        "Bearer " + localStorage.getItem("accessToken");

      res = await fetch(url, options);
    }

    // 👇 SI SIGUE 401, recién ahí
    if (res.status === 401) {
      console.warn("HTTP 401 definitivo");

      forceLogout();
      throw new Error("unauthorized");
    }
  }

  return res;
}

/* ================= WEBSOCKET ================= */
export async function connectWSWithAuth(connectFn) {
  let token = localStorage.getItem("accessToken");

  if (await connectFn(token)) return true;

  const refreshed = await tryRefreshToken();
  if (!refreshed) {
    console.warn("WS refresh falló, no logout");
    return false;
  }

  token = localStorage.getItem("accessToken");
  return await connectFn(token);
}
