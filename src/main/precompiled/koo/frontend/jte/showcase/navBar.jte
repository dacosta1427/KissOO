@param boolean loggedIn

<header class="app-nav" data-signals="{theme:(localStorage.getItem('theme')||'light')}"
        data-effect="document.documentElement.setAttribute('data-theme', localStorage.getItem('theme')||'light')">
  <a class="brand" href="/showcase">
    <span class="brand-mark">K</span>
    <span class="brand-name">KissOO</span>
  </a>

  <nav class="fast-nav" aria-label="Primary">
    <a class="nav-link" href="/showcase/controls" data-class:active="location.pathname==='/showcase/controls'">Controls</a>
@if(loggedIn)
    <a class="nav-link" href="/showcase/crud" data-class:active="location.pathname==='/showcase/crud'">Owners</a>
@endif
    <a class="nav-link" href="/showcase/datastar" data-class:active="location.pathname==='/showcase/datastar'">Datastar</a>
    <a class="nav-link" href="/showcase/sse" data-class:active="location.pathname==='/showcase/sse'">Live SSE</a>
  </nav>

  <div class="nav-actions">
@if(loggedIn)
    <span class="nav-user" data-text="localStorage.getItem('kissUser') || ''"></span>
@endif

    <button class="theme-toggle" type="button" aria-label="Toggle light / dark theme"
            data-on:click="$theme = $theme==='dark'?'light':'dark'; localStorage.setItem('theme',$theme); document.documentElement.setAttribute('data-theme',$theme)">
      <span data-text="$theme==='dark'?'🌙':'☀️'"></span>
    </button>

    <details class="dropdown hamburger">
      <summary aria-label="Open menu">☰</summary>
      <ul>
        <li><a href="/showcase/auth">Auth demo</a></li>
        <li><a href="/showcase/htmx">HTMX demo</a></li>
        <li><a href="/showcase/login">Login screen</a></li>
        <li><a href="/showcase">Home</a></li>
@if(loggedIn)
        <li class="dropdown-divider" role="separator"></li>
        <li><a href="/rest?_class=services/ShowcaseService&_method=logout">Logout</a></li>
@endif
      </ul>
    </details>
  </div>
</header>
