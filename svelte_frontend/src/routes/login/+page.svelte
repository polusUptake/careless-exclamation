<script lang="ts">
	let username = '';
	let password = '';
	let errorMessage = '';

	async function handleLogin(event: SubmitEvent) {
		event.preventDefault();
		errorMessage = '';

		try {
			const response = await fetch('http://localhost:8080/api/login', {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify({ username, password })
			});

			if (!response.ok) {
				errorMessage = 'Login request failed';
				return;
			}

			const data = await response.json();

			if (data.success) {
				window.location.href = '/dash';
				return;
			}

			errorMessage = 'Invalid username or password';
		} catch {
			errorMessage = 'Could not connect to backend';
		}
	}
</script>

<svelte:head>
	<title>Login</title>
	<link rel="preconnect" href="https://fonts.googleapis.com" />
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="anonymous" />
	<link
		href="https://fonts.googleapis.com/css2?family=Funnel+Display:wght@400;500;600;700&display=swap"
		rel="stylesheet"
	/>
</svelte:head>

<main class="login-page">

    <h1>NBA COURSE FILE GENERATOR + CO ATTAINMENT CALCULATOR</h1>
	<section class="card">
		<h1 id="login-title">Welcome</h1>
		<p class="subtitle">Enter username and password</p>

		<form class="login-form" on:submit={handleLogin}>
			<div class="field-group">
				<label for="username">Username</label>
				<input
					id="username"
					name="username"
					type="text"
					bind:value={username}
					required
				/>
			</div>

			<div class="field-group">
				<label for="password">Password</label>
				<input
					id="password"
					name="password"
					type="password"
					bind:value={password}
					required
				/>
			</div>

			<button type="submit">Log In</button>
			{#if errorMessage}
				<p class="error-message">{errorMessage}</p>
			{/if}
		</form>
	</section>
</main>

<style>
	:global(:root) {
		--blue-950: #060b18;
		--blue-900: #0b142a;
		--blue-800: #132344;
		--blue-700: #1c325f;
		--blue-500: #4f73bd;
		--blue-300: #9db4e6;
		--text-100: #edf3ff;
		--text-200: #cad8f3;
	}

	:global(body) {
		margin: 0;
		min-height: 100vh;
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		background:
			radial-gradient(circle at 14% 18%, rgba(79, 115, 189, 0.2), transparent 36%),
			radial-gradient(circle at 86% 82%, rgba(157, 180, 230, 0.12), transparent 42%),
			linear-gradient(145deg, var(--blue-950) 0%, var(--blue-900) 48%, #040813 100%);
		color: var(--text-100);
	}

	.login-page {
		min-height: 100vh;
		display: grid;
		place-items: center;
		padding: 1.25rem;
	}

	.card {
        margin-top: -10%;
		width: min(28rem, 100%);
		padding: 2rem;
		border-radius: 1.1rem;
		background: linear-gradient(165deg, rgba(12, 23, 46, 0.9), rgba(8, 15, 31, 0.96));
		border: 1px solid rgba(79, 115, 189, 0.45);
		box-shadow:
			0 18px 42px rgba(0, 0, 0, 0.45),
			inset 0 0 0 1px rgba(157, 180, 230, 0.14);
		backdrop-filter: blur(5px);
	}

	h1 {
		margin: 0.35rem 0 0;
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: clamp(1.9rem, 5vw, 2.4rem);
		font-weight: 700;
		letter-spacing: 0.01em;
		color: var(--text-100);
	}

	.subtitle {
		margin: 0.55rem 0 1.4rem;
		color: var(--text-200);
		font-size: 0.98rem;
	}

	.login-form {
		display: grid;
		gap: 1rem;
	}

	.field-group {
		display: grid;
		gap: 0.5rem;
	}

	label {
		font-size: 0.9rem;
		font-weight: 600;
		color: #d5e2fb;
	}

	input {
		width: 100%;
		padding: 0.72rem 0.85rem;
		border-radius: 0.6rem;
		border: 1px solid rgba(79, 115, 189, 0.42);
		background: rgba(5, 11, 26, 0.76);
		color: var(--text-100);
		font-size: 0.95rem;
		outline: none;
		transition:
			border-color 0.2s ease,
			box-shadow 0.2s ease,
			transform 0.2s ease;
	}

	input::placeholder {
		color: rgba(202, 216, 243, 0.55);
	}

	button {
		margin-top: 0.35rem;
		padding: 0.8rem 1rem;
		border: 0;
		border-radius: 0.65rem;
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: 0.98rem;
		font-weight: 600;
		letter-spacing: 0.04em;
		cursor: pointer;
		color: #eaf1ff;
		background: var(--blue-700);
		transition:
			transform 0.2s ease,
			box-shadow 0.2s ease,
			filter 0.2s ease;
	}

	button:hover {
		background: var(--blue-500);
		box-shadow: 0 10px 22px rgba(20, 41, 90, 0.45);
		filter: saturate(1.05);
	}

	.error-message {
		margin: 0.35rem 0 0;
		color: #ffb4b4;
		font-size: 0.9rem;
	}


	@media (max-width: 460px) {
		.card {
			padding: 1.4rem;
		}

		h1 {
			font-size: 1.7rem;
		}
	}
</style>
