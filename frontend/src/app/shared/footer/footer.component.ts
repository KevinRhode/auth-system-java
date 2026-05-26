import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  styleUrl: './footer.component.scss',
  template: `
    <footer class="footer">
      <div class="footer-inner">

        <div class="footer-brand">
          <span class="brand-name">AuthSystem</span>
          <p class="brand-tagline">A full stack auth demo built with Angular, Spring Boot & PostgreSQL.</p>
        </div>

        <div class="footer-sections">

          <div class="footer-section">
            <h4>Social</h4>
            <ul>
              <li><a href="https://github.com/" target="_blank" rel="noopener">GitHub</a></li>
              <li><a href="https://linkedin.com/" target="_blank" rel="noopener">LinkedIn</a></li>
              <li><a href="https://twitter.com/" target="_blank" rel="noopener">Twitter / X</a></li>
            </ul>
          </div>

          <div class="footer-section">
            <h4>Writing</h4>
            <ul>
              <li><a href="https://medium.com/" target="_blank" rel="noopener">Blog</a></li>
              <li><a href="#" target="_blank" rel="noopener">Newsletter</a></li>
              <li><a href="#" target="_blank" rel="noopener">eBook</a></li>
            </ul>
          </div>

          <div class="footer-section">
            <h4>Resources</h4>
            <ul>
              <li><a href="https://angular.dev" target="_blank" rel="noopener">Angular</a></li>
              <li><a href="https://spring.io/projects/spring-boot" target="_blank" rel="noopener">Spring Boot</a></li>
              <li><a href="https://neon.tech" target="_blank" rel="noopener">Neon</a></li>
              <li></li>
            </ul>
          </div>

        </div>
      </div>

      <div class="footer-bottom">
        <p>© {{ year }} AuthSystem. All rights reserved.</p>
        <p class="attribution">
          Icons by <a href="https://www.flaticon.com/free-icons/authentication" title="authentication icons">Flaticon</a>
          
        </p>
      </div>
    </footer>
  `
})
export class FooterComponent {
  year = new Date().getFullYear();
}