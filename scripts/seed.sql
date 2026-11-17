INSERT INTO titles (title, media_type, total_episodes_chapters)
SELECT seed.title, seed.media_type, seed.total
FROM (
    VALUES
        ('Attack on Titan', 'MANGA', 139),
        ('Cowboy Bebop', 'ANIME', 26),
        ('Death Note', 'ANIME', 37),
        ('Death Note', 'MANGA', 108),
        ('Demon Slayer: Kimetsu no Yaiba', 'MANGA', 205),
        ('Fullmetal Alchemist', 'MANGA', 116),
        ('Fullmetal Alchemist: Brotherhood', 'ANIME', 64),
        ('Hunter x Hunter', 'ANIME', 148),
        ('Naruto', 'ANIME', 220),
        ('Neon Genesis Evangelion', 'ANIME', 26),
        ('Samurai Champloo', 'ANIME', 26),
        ('Steins;Gate', 'ANIME', 24)
) AS seed(title, media_type, total)
WHERE NOT EXISTS (
    SELECT 1
    FROM titles existing
    WHERE LOWER(existing.title) = LOWER(seed.title)
      AND existing.media_type = seed.media_type
);
