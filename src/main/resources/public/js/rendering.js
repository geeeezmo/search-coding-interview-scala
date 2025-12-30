import { escapeHtml } from './helpers.js'

export function renderSidebar(facets, filters) {
    const sidebar = document.getElementById('sidebar')

    const generateHTML = (field) => `<div class="facet">
        <div class="facet-header">
            ${field}
        </div>
        ${facets?.[field]?.map(value => {
            return `<label>
                <input
                    type="checkbox"
                    data-facet="${escapeHtml(field)}"
                    value="${escapeHtml(value.value)}"
                    ${filters[field]?.includes(value.value) ? 'checked="checked"': ''}
                />
                ${escapeHtml(value.value)} (${escapeHtml(value.count)})
            </label>`
        })?.join('') ?? ''}
    </div>`

    sidebar.innerHTML = `
        ${generateHTML('price')}
        ${generateHTML('year')}
    `
}

export function renderContent(documents) {
    const content = document.getElementById('content')

    var formatter = new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    })

    content.innerHTML = `
        Found ${escapeHtml(documents.length)} documents
        ${Array.from(documents).map(document => {
            return `<div class="document">
                <div class="title" >
                    <img src="${escapeHtml(document.image)}"/>
                    ${escapeHtml(document.title)}
                </div>
                <div class="price">${formatter.format(document.price)}</div>
            </div>`
        }).join('')}
    `
}

export function bindFacetChange(onChange) {
    const facetElements = [...document.querySelectorAll('input[data-facet]')]
    facetElements.forEach(element => {
        element.addEventListener('click', () => {
            const values = facetElements.reduce((prev, cur) => {
                if (!cur.checked) {
                    return prev
                }

                const field = cur.getAttribute('data-facet') || ''
                const value = cur.value || ''

                if (!(field in prev)) {
                    prev[field] = []
                }
                prev[field].push(value)

                return prev
            }, {})

            onChange(values)
        })
    })
}
