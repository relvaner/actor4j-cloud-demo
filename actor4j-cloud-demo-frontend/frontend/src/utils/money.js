// MoneyDTO (currency_code, units, nanos) -> e.g. "$124.00"
export function formatMoney(money) {
	if (!money)
		return '';

	const amount = Number(money.units) + Number(money.nanos) / 1e9;
	try {
		return new Intl.NumberFormat('en-US', { style: 'currency', currency: money.currency_code }).format(amount);
	}
	catch (e) {
		return money.currency_code + ' ' + amount.toFixed(2);
	}
}
