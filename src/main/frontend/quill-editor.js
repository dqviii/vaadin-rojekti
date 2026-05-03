import Quill from 'quill';
import 'quill/dist/quill.snow.css';

class QuillEditor extends HTMLElement {
    static get observedAttributes() {
        return ['placeholder'];
    }

    constructor() {
        super();
        this._value = '';
    }

    connectedCallback() {
        if (this._initialized) return;
        this._initialized = true;

        this.style.display = 'block';

        const container = document.createElement('div');
        container.style.minHeight = '160px';
        this.appendChild(container);

        this._quill = new Quill(container, {
            theme: 'snow',
            placeholder: this.getAttribute('placeholder') || '',
            modules: {
                toolbar: [
                    ['bold', 'italic', 'underline'],
                    [{ list: 'ordered' }, { list: 'bullet' }],
                    ['link', 'clean']
                ]
            }
        });

        if (this._value) {
            this._quill.root.innerHTML = this._value;
        }

        this._quill.on('text-change', () => {
            const html = this._quill.root.innerHTML;
            if (html === this._value) return;
            this._value = html;
            this.dispatchEvent(new CustomEvent('value-changed', {
                bubbles: true,
                composed: true
            }));
        });
    }

    get value() {
        return this._value;
    }

    set value(v) {
        const next = (v == null) ? '' : String(v);
        if (next === this._value) return;
        this._value = next;
        if (this._quill && this._quill.root.innerHTML !== next) {
            this._quill.root.innerHTML = next;
        }
    }
}

customElements.define('quill-editor', QuillEditor);
