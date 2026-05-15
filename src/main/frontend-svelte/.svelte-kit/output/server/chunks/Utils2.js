import "clsx";
function toInputDateFormat(yyyymmdd) {
  if (!yyyymmdd || yyyymmdd.length !== 8) return "";
  const year = yyyymmdd.substring(0, 4);
  const month = yyyymmdd.substring(4, 6);
  const day = yyyymmdd.substring(6, 8);
  return `${year}-${month}-${day}`;
}
function toDisplayDateFormat(yyyymmdd, locale = "en") {
  if (!yyyymmdd || yyyymmdd.length !== 8) return "";
  const year = parseInt(yyyymmdd.substring(0, 4));
  const month = parseInt(yyyymmdd.substring(4, 6)) - 1;
  const day = parseInt(yyyymmdd.substring(6, 8));
  const date = new Date(year, month, day);
  return date.toLocaleDateString(locale, { month: "short", day: "numeric", year: "numeric" });
}
export {
  toInputDateFormat as a,
  toDisplayDateFormat as t
};
