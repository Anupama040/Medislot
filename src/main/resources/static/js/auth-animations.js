document.addEventListener("DOMContentLoaded", () => {
    // 1. Anime.js Staggered Slide-In
    const elementsToAnimate = [
        '.auth-header-exact img', '.auth-white-card', '#toast-container'
    ];
    
    if (typeof anime !== 'undefined') {
        anime({
            targets: elementsToAnimate,
            translateY: [20, 0],
            opacity: [0, 1],
            easing: 'easeOutElastic(1, .8)',
            duration: 800,
            delay: anime.stagger(80, {start: 100})
        });

        // Tagline Letter-by-Letter Animation
        const tagline = document.querySelector('.brand-tagline');
        if (tagline) {
            const text = tagline.innerText;
            tagline.innerHTML = text.split('').map(char => 
                char === ' ' ? '&nbsp;' : `<span class="letter" style="opacity: 0; display: inline-block;">${char}</span>`
            ).join('');
            
            anime({
                targets: '.brand-tagline .letter',
                opacity: [0, 1],
                translateY: [5, 0],
                delay: anime.stagger(25, {start: 400})
            });
        }

        // Heartbeat Pulse Animation
        anime({
            targets: '.heartbeat-icon',
            scale: [1, 1.15, 1],
            easing: 'easeInOutSine',
            duration: 800,
            loop: true
        });

        // Illustration float
        anime({
            targets: '.main-illustration',
            translateY: [-10, 10],
            direction: 'alternate',
            loop: true,
            easing: 'easeInOutSine',
            duration: 2500
        });
    }

    // 2. Doctor Access Toggle Logic
    const doctorAccessBtn = document.getElementById('doctorAccessBtn');
    const doctorAccessLabel = document.getElementById('doctorAccessLabel');
    const userRoleInput = document.getElementById('userRole');
    const dynamicSignupLink = document.getElementById('dynamicSignupLink');
    const emailInput = document.getElementById('emailInput');

    if (doctorAccessBtn) {
        doctorAccessBtn.addEventListener('click', () => {
            if (userRoleInput.value === 'patient') {
                userRoleInput.value = 'doctor';
                doctorAccessBtn.style.color = '#10b981';
                doctorAccessLabel.style.display = 'block';
                emailInput.placeholder = 'doctor@medislot.com';
                if(dynamicSignupLink) {
                    dynamicSignupLink.href = '/register/doctor';
                    dynamicSignupLink.textContent = 'Apply as Doctor';
                }
            } else {
                userRoleInput.value = 'patient';
                doctorAccessBtn.style.color = '#94a3b8';
                doctorAccessLabel.style.display = 'none';
                emailInput.placeholder = 'Enter your email';
                if(dynamicSignupLink) {
                    dynamicSignupLink.href = '/register/patient';
                    dynamicSignupLink.textContent = 'Sign up as Patient';
                }
            }
        });
    }

    // 3. Password Visibility Toggle
    const togglePassword = document.getElementById('togglePassword');
    const passwordInput = document.getElementById('passwordInput');

    if (togglePassword && passwordInput) {
        togglePassword.addEventListener('click', () => {
            const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
            passwordInput.setAttribute('type', type);
            
            // Toggle icon
            const icon = togglePassword.querySelector('i');
            if (type === 'text') {
                icon.classList.remove('fa-eye');
                icon.classList.add('fa-eye-slash');
                icon.style.color = '#3b82f6';
            } else {
                icon.classList.remove('fa-eye-slash');
                icon.classList.add('fa-eye');
                icon.style.color = '#94a3b8';
            }
        });
    }

    // 3.5 Loading Spinner on Submit
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', () => {
            const btnText = document.getElementById('loginBtnText');
            const spinner = document.getElementById('loginBtnSpinner');
            if(btnText && spinner) {
                btnText.style.opacity = '0';
                spinner.style.display = 'block';
            }
        });
    }

    // 4. Parallax effect for background shapes
    const shapes = document.querySelectorAll('.bg-shape, .bg-pattern-icons i');
    document.addEventListener('mousemove', (e) => {
        const x = e.clientX / window.innerWidth;
        const y = e.clientY / window.innerHeight;
        
        shapes.forEach((shape, index) => {
            const speed = (index % 3 + 1) * 20; 
            const moveX = (x * speed) - (speed/2);
            const moveY = (y * speed) - (speed/2);
            shape.style.transform = `translate(${moveX}px, ${moveY}px)`;
        });
    });

    // 5. Auto-fade Toast Notifications after 5 seconds
    const toasts = document.querySelectorAll('.toast');
    if (toasts.length > 0) {
        setTimeout(() => {
            toasts.forEach(toast => {
                toast.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
                toast.style.opacity = '0';
                toast.style.transform = 'translateY(-20px)';
                setTimeout(() => toast.remove(), 500); // Remove from DOM after transition
            });
        }, 5000);
    }
});
