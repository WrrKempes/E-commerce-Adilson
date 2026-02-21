-- Seed catálogo Adylson Machado

INSERT INTO books (id, author, category, description, image_url, price, stock, title, author_id)
VALUES
    (
        'amendoeiras',
        'Adylson Machado',
        'cronica',
        'Amendoeiras de Outono',
        'https://www.adylsonmachado.com.br/images/amendoeiras.webp',
        60.00,
        0,
        'Amendoeiras de Outono',
        3
    ),
    (
        'lambe',
        'Adylson Machado',
        'contos',
        'Lambe-lambe e outros contos',
        'https://viaeditora.com.br/wp-content/uploads/2021/02/lambe-lambe.jpg',
        25.00,
        6,
        'Lambe-lambe e outros contos',
        3
    ),
    (
        'cinza',
        'Adylson Machado',
        'cronica',
        'O cinza e o silêncio',
        'https://viaeditora.com.br/wp-content/uploads/2021/02/o-cinza-e-o-silencio.jpg',
        25.00,
        5,
        'O cinza e o silêncio',
        3
    ),
    (
        'burro',
        'Adylson Machado',
        'cronica',
        'Chama o burro e outras crônicas de antanho',
        'https://viaeditora.com.br/wp-content/uploads/2021/02/chama-o-burro.jpg',
        30.00,
        7,
        'Chama o burro e outras crônicas de antanho',
        3
    ),
    (
        'ambar',
        'Adylson Machado',
        'cronica',
        'Entre nuvens de âmbar: o sonho tirano insiste e persegue',
        'https://viaeditora.com.br/wp-content/uploads/2021/02/entre-nuvens-de-ambar.jpg',
        25.00,
        6,
        'Entre nuvens de âmbar: o sonho tirano insiste e persegue',
        3
    ),
    (
        'portal',
        'Adylson Machado',
        'cronica',
        'Portal da Piedade',
        'https://viaeditora.com.br/wp-content/uploads/2021/02/portal-da-piedade.jpg',
        30.00,
        0,
        'Portal da Piedade',
        3
    ),
    (
        'abc',
        'Adylson Machado',
        'cronica',
        'O abc do Cabôco',
        'https://viaeditora.com.br/wp-content/uploads/2021/02/portal-da-piedade.jpg',
        30.00,
        0,
        'O abc do Cabôco',
        3
    )
    ON CONFLICT (id) DO UPDATE
                            SET
                                author      = EXCLUDED.author,
                            category    = EXCLUDED.category,
                            description = EXCLUDED.description,
                            image_url   = EXCLUDED.image_url,
                            price       = EXCLUDED.price,
                            stock       = EXCLUDED.stock,
                            title       = EXCLUDED.title,
                            author_id   = EXCLUDED.author_id;


-- vínculo para pagamento (autor 3)
INSERT INTO payment_book_authors (book_id, author_id)
SELECT b.id, 3
FROM books b
WHERE b.id IN ('amendoeiras','lambe','cinza','burro','ambar','portal','abc')
    ON CONFLICT (book_id) DO UPDATE
                                 SET author_id = EXCLUDED.author_id;