-- Rode UMA vez no Supabase: SQL Editor > New query > Run
create table if not exists public.jogadores (
  id bigint generated always as identity primary key,
  nome text not null unique
);

create table if not exists public.partidas (
  id bigint generated always as identity primary key,
  jogador_id bigint not null references public.jogadores(id),
  kills integer not null,
  tempo_segundos integer not null
);

create index if not exists partidas_ranking_idx
  on public.partidas (kills desc, tempo_segundos desc);

alter table public.jogadores enable row level security;
alter table public.partidas  enable row level security;

-- A chave "anon" pode ler e inserir (e o upsert de jogador precisa de update).
create policy "jogadores_select" on public.jogadores for select using (true);
create policy "jogadores_insert" on public.jogadores for insert with check (true);
create policy "jogadores_update" on public.jogadores for update using (true) with check (true);
create policy "partidas_select"  on public.partidas  for select using (true);
create policy "partidas_insert"  on public.partidas  for insert with check (true);
