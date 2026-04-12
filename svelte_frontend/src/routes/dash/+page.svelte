<script lang="ts">
	function handleLogout() {
		window.location.href = '/login';
	}
</script>

<svelte:head>
	<title>Dashboard</title>
	<link rel="preconnect" href="https://fonts.googleapis.com" />
	<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin="anonymous" />
	<link
		href="https://fonts.googleapis.com/css2?family=Funnel+Display:wght@400;500;600;700&display=swap"
		rel="stylesheet"
	/>
</svelte:head>

<main class="dash-page">
	<section class="card">
		<div class="title-row">
			<h1 id="dash-title">Create Course File</h1>
			<button type="button" class="logout-button" on:click={handleLogout}>Log Out</button>
		</div>

		<div class="controls-row" role="group">
			<p>Name Of Instructor : Dr. Amrita Naik</p>
			<p>Dept : Computer Engineering</p>

            <p>Course:</p>
			<select>
				<option value="" selected disabled>Select option</option>
				<option value="placeholder-a">Object Oriented Programming Systems</option>
				<option value="placeholder-b">Data Structures</option>
				<option value="placeholder-c">C++</option>
			</select>

            <p>Tool:</p>
			<select>
				<option value="" selected disabled>Select option</option>
                <option value="placeholder-d">Assignment</option>
				<option value="placeholder-e">Internal Test</option>
				<option value="placeholder-f">Semester End Exam</option>
				<option value="placeholder-g">Course Exit Survey</option>
			</select>
		</div>

	</section>

	<section class="display-box">
		<h1 class="display-title">Import files</h1>
		<div class="text-boxes">
			<button type="button" class="text-box">Internal Test 1</button>
			<button type="button" class="text-box">Internal Test 2</button>
			<button type="button" class="text-box">Internal Test 3</button>
			<button type="button" class="text-box">Semester Exam</button>
			<button type="button" class="text-box">Course Exit Survey</button>
		</div>
	</section>

	<section class="actions-panel">
		<div class="actions-row">
			<div class="calculate-controls">
				<select aria-label="Calculate options">
					<option value="" selected disabled>Select option</option>
					<option value="calculate-co">Calculate CO</option>
					<option value="calculate-po">Calculate PO</option>
				</select>
				<button type="button" class="calculate-button">Calculate</button>
			</div>

			<div class="print-controls">
				<button type="button" class="print-button">Print</button>
			</div>
		</div>
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

	.dash-page {
		min-height: 100vh;
		display: grid;
		align-content: start;
		gap: 1rem;
		padding: 1.25rem;
	}

	.card {
		width: min(100rem, 100%);
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
		margin: 0;
		font-size: clamp(1.7rem, 4vw, 2.2rem);
		font-weight: 700;
		letter-spacing: 0.01em;
		color: var(--text-100);
	}

	.title-row {
		display: flex;
		align-items: center;
		gap: 1rem;
	}

	.logout-button {
		margin-left: auto;
		padding: 0.62rem 1rem;
		border: 0;
		border-radius: 0.6rem;
		background: #c62828;
		color: #ffffff;
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: 0.95rem;
		font-weight: 600;
		cursor: pointer;
		transition:
			transform 0.2s ease,
			box-shadow 0.2s ease,
			background-color 0.2s ease;
	}

	.logout-button:hover {
		background: #e53935;
		box-shadow: 0 10px 20px rgba(198, 40, 40, 0.3);
		transform: translateY(-1px);
	}

	.logout-button:active {
		transform: translateY(0);
	}

	.controls-row {
		display: flex;
		align-items: center;
        padding:0.8rem;
		gap: 1.8rem;
		flex-wrap: wrap;
	}

	select {
		min-width: 12rem;
		padding: 0.68rem 0.8rem;
		border-radius: 0.6rem;
		border: 1px solid rgba(79, 115, 189, 0.42);
		background: rgba(5, 11, 26, 0.76);
		color: var(--text-100);
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: 0.94rem;
		outline: none;
		transition:
			border-color 0.2s ease,
			box-shadow 0.2s ease,
			transform 0.2s ease;
	}

	select:focus {
		border-color: var(--blue-300);
		box-shadow: 0 0 0 3px rgba(79, 115, 189, 0.24);
		transform: translateY(-1px);
	}

	.actions-row {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-wrap: wrap;
		gap: 1rem;
	}

	.actions-panel {
		width: min(100rem, 100%);
		padding: 1rem 1.3rem;
		border-radius: 1.1rem;
		background: linear-gradient(165deg, rgba(9, 19, 38, 0.88), rgba(7, 13, 28, 0.95));
		border: 1px solid rgba(79, 115, 189, 0.35);
		box-shadow: 0 14px 34px rgba(0, 0, 0, 0.35);
	}

	.calculate-controls {
		display: inline-flex;
		align-items: center;
		gap: 0.6rem;
	}

	.print-controls {
		display: inline-flex;
		align-items: center;
		gap: 0.6rem;
	}

	.print-button {
		padding: 0.72rem 1.1rem;
		border: 0;
		border-radius: 0.6rem;
		background: var(--blue-700);
		color: #eaf1ff;
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: 0.95rem;
		font-weight: 600;
		letter-spacing: 0.02em;
		cursor: pointer;
		transition:
			transform 0.2s ease,
			box-shadow 0.2s ease,
			background-color 0.2s ease;
	}

	.print-button:hover {
		background: var(--blue-500);
		box-shadow: 0 10px 20px rgba(20, 41, 90, 0.35);
		transform: translateY(-1px);
	}

	.print-button:active {
		transform: translateY(0);
	}

	.calculate-button {
		padding: 0.72rem 1.1rem;
		border: 0;
		border-radius: 0.6rem;
		background: var(--blue-800);
		color: #eaf1ff;
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: 0.95rem;
		font-weight: 600;
		letter-spacing: 0.02em;
		cursor: pointer;
		transition:
			transform 0.2s ease,
			box-shadow 0.2s ease,
			background-color 0.2s ease;
	}

	.calculate-button:hover {
		background: var(--blue-700);
		box-shadow: 0 10px 20px rgba(20, 41, 90, 0.35);
		transform: translateY(-1px);
	}

	.calculate-button:active {
		transform: translateY(0);
	}

	.display-box {
		width: min(100rem, 100%);
		padding: 1.3rem;
		border-radius: 1.1rem;
		background: linear-gradient(165deg, rgba(9, 19, 38, 0.88), rgba(7, 13, 28, 0.95));
		border: 1px solid rgba(79, 115, 189, 0.35);
		box-shadow: 0 14px 34px rgba(0, 0, 0, 0.35);
	}

	.display-title {
		margin: 0 0 0.9rem;
		font-size: 1.1rem;
		font-weight: 600;
		letter-spacing: 0.01em;
		color: var(--text-100);
	}

	.text-boxes {
		display: grid;
		grid-template-columns: repeat(5, minmax(0, 1fr));
		gap: 1rem;
	}

	.text-box {
		min-height: 10rem;
		display: grid;
		place-items: center;
		width: 100%;
		padding: 0;
		border-radius: 0.9rem;
		border: 1px solid rgba(157, 180, 230, 0.28);
		background: linear-gradient(170deg, rgba(17, 34, 68, 0.86), rgba(11, 21, 44, 0.95));
		color: var(--text-100);
		font-family: 'Funnel Display', 'Segoe UI', sans-serif;
		font-size: 1.05rem;
		font-weight: 600;
		letter-spacing: 0.02em;
		cursor: pointer;
	}

	@media (max-width: 760px) {
		.card {
			padding: 1.4rem;
		}

		.controls-row {
			align-items: stretch;
		}

		select {
			width: 100%;
			min-width: 0;
		}

		.actions-row {
			justify-content: center;
			align-items: center;
		}

		.text-boxes {
			grid-template-columns: 1fr;
		}

		.calculate-controls,
		.print-controls {
			justify-self: start;
		}

		.calculate-controls {
			width: 100%;
		}

		.print-controls {
			width: 100%;
		}

		.print-button {
			flex: 0 0 auto;
		}
	}
</style>
