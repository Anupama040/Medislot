document.addEventListener("DOMContentLoaded", () => {
    // Check if anime.js is loaded
    if (typeof anime === 'undefined') {
        console.warn("Anime.js is not loaded.");
        return;
    }

    // 1. Topbar Slide In
    anime({
        targets: '.topbar',
        translateY: [-50, 0],
        opacity: [0, 1],
        easing: 'easeOutExpo',
        duration: 1000
    });

    // 2. Staggered fade and slide for headers, alerts, and cards
    const elementsToAnimate = [
        '.welcome-header',
        '.alert',
        '.panel',
        '.modern-card',
        '.ai-search-card',
        '.modern-doctor-card',
        'table tr'
    ];

    // Filter elements that actually exist on the current page
    const existingElements = [];
    elementsToAnimate.forEach(selector => {
        const els = document.querySelectorAll(selector);
        els.forEach(el => existingElements.push(el));
    });

    if (existingElements.length > 0) {
        anime({
            targets: existingElements,
            translateY: [20, 0],
            opacity: [0, 1],
            easing: 'easeOutQuart',
            duration: 800,
            delay: anime.stagger(100, {start: 300}) // Stagger by 100ms, start after 300ms
        });
    }

    // 3. Float animation for notification illustration (if exists)
    const notificationImg = document.querySelector('.notification-illustration img');
    if (notificationImg) {
        anime({
            targets: notificationImg,
            translateY: [-8, 8],
            direction: 'alternate',
            loop: true,
            easing: 'easeInOutSine',
            duration: 3000
        });
    }

    // 4. Parallax effect for background shapes/icons on mouse move
    const patternIcons = document.querySelectorAll('.dash-pattern i');
    if (patternIcons.length > 0) {
        document.addEventListener('mousemove', (e) => {
            const x = e.clientX / window.innerWidth;
            const y = e.clientY / window.innerHeight;
            
            patternIcons.forEach((icon, index) => {
                const speed = (index % 3 + 1) * 15; 
                const moveX = (x * speed) - (speed/2);
                const moveY = (y * speed) - (speed/2);
                
                // Using standard style assignment for better performance on mousemove than anime.js
                icon.style.transform = `translate(${moveX}px, ${moveY}px)`;
            });
        });
    }

    // 5. Button hover ripple effect (attach to modern buttons)
    const modernBtns = document.querySelectorAll('.button:not(.ghost)');
    modernBtns.forEach(btn => {
        btn.addEventListener('mouseenter', function(e) {
            anime({
                targets: this,
                scale: 1.05,
                duration: 200,
                easing: 'easeOutSine'
            });
        });
        btn.addEventListener('mouseleave', function(e) {
            anime({
                targets: this,
                scale: 1,
                duration: 200,
                easing: 'easeOutSine'
            });
        });
    });

    // 6. Sticky Navbar Scroll Shrink Effect
    const topbar = document.querySelector('.topbar');
    if (topbar) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 20) {
                topbar.classList.add('scrolled');
            } else {
                topbar.classList.remove('scrolled');
            }
        });
        // Run once on load just in case the page is already scrolled
        if (window.scrollY > 20) {
            topbar.classList.add('scrolled');
        }
    }
});
