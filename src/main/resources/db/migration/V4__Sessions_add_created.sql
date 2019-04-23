UPDATE SESSIONS SET DATA = DATA || jsonb_build_object('created', now() :: timestamp);
