-- Seed employers for restructured employers table
INSERT INTO employers (id, user_id, company_name, description, inn, website, socials, logo_url, status)
SELECT 1, NULL, 'Acme Corp', 'Global software company', '7701234567', 'https://acme.com', 'hr@acme.com', 'https://acme.com/logo.png', 'verified'
WHERE EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'employers'
      AND column_name = 'company_name'
)
ON CONFLICT (id) DO NOTHING;

-- Seed employers for legacy employers table
INSERT INTO employers (id, name, logo_url, website, contacts)
SELECT 1, 'Acme Corp', 'https://acme.com/logo.png', 'https://acme.com', 'hr@acme.com'
WHERE NOT EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_name = 'employers'
      AND column_name = 'company_name'
)
ON CONFLICT (id) DO NOTHING;

-- Seed tags: technologies
INSERT INTO tags (id, name, category) VALUES
    (1,  'Python',      'TECHNOLOGY'),
    (2,  'Java',        'TECHNOLOGY'),
    (3,  'SQL',         'TECHNOLOGY'),
    (4,  'JavaScript',  'TECHNOLOGY'),
    (5,  'TypeScript',  'TECHNOLOGY'),
    (6,  'Go',          'TECHNOLOGY'),
    (7,  'Kotlin',      'TECHNOLOGY'),
    (8,  'C++',         'TECHNOLOGY'),
    (9,  'React',       'TECHNOLOGY'),
    (10, 'Spring Boot', 'TECHNOLOGY'),
-- Seed tags: levels
    (11, 'Junior',      'LEVEL'),
    (12, 'Middle',      'LEVEL'),
    (13, 'Senior',      'LEVEL'),
    (14, 'Intern',      'LEVEL'),
-- Seed tags: employment types
    (15, 'Full-time',   'EMPLOYMENT_TYPE'),
    (16, 'Part-time',   'EMPLOYMENT_TYPE'),
    (17, 'Project',     'EMPLOYMENT_TYPE'),
    (18, 'Freelance',   'EMPLOYMENT_TYPE')
ON CONFLICT (id) DO NOTHING;

-- Sync sequences so Hibernate-generated IDs don't collide with seeded rows
SELECT setval('employers_id_seq', (SELECT MAX(id) FROM employers));
SELECT setval('tags_id_seq',      (SELECT MAX(id) FROM tags));
