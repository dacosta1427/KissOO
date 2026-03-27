import "../../../chunks/async.js";
import { e as ensure_array_like, b as attr_class, u as unsubscribe_stores, s as store_get } from "../../../chunks/index2.js";
import { t, c as currentLocale } from "../../../chunks/index3.js";
import { a as attr, e as escape_html } from "../../../chunks/attributes.js";
function _page($$renderer, $$props) {
  $$renderer.component(($$renderer2) => {
    var $$store_subs;
    const tt = (key) => t(key, void 0, store_get($$store_subs ??= {}, "$currentLocale", currentLocale));
    let costProfiles = [];
    let owners = [];
    function getOwnerName(ownerId) {
      if (!ownerId) return "Global";
      const owner = owners.find((o) => o.id === ownerId);
      return owner?.name || "Unknown";
    }
    $$renderer2.push(`<div class="cost-profiles-page svelte-p07anx"><div class="page-header svelte-p07anx"><h1 class="svelte-p07anx">Cost Profiles</h1> <button class="btn btn-primary"${attr("title", tt("hints.add_new"))}>${escape_html(tt("cost_profiles.add_new"))}</button></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]--> `);
    {
      $$renderer2.push("<!--[-1-->");
      $$renderer2.push(`<div class="profiles-grid svelte-p07anx"><!--[-->`);
      const each_array = ensure_array_like(costProfiles);
      for (let $$index = 0, $$length = each_array.length; $$index < $$length; $$index++) {
        let profile = each_array[$$index];
        $$renderer2.push(`<div${attr_class("profile-card svelte-p07anx", void 0, { "standard": profile.is_standard })}><div class="profile-header svelte-p07anx"><h3 class="svelte-p07anx">${escape_html(profile.name)}</h3> `);
        if (profile.is_standard) {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<span class="badge badge-standard svelte-p07anx">Standard</span>`);
        } else {
          $$renderer2.push("<!--[-1-->");
        }
        $$renderer2.push(`<!--]--> `);
        if (!profile.active) {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<span class="badge badge-inactive svelte-p07anx">Inactive</span>`);
        } else {
          $$renderer2.push("<!--[-1-->");
        }
        $$renderer2.push(`<!--]--></div> <div class="profile-owner svelte-p07anx"><strong>Owner:</strong> ${escape_html(getOwnerName(profile.owner))}</div> <div class="profile-rates svelte-p07anx"><div class="rate-group svelte-p07anx"><h4 class="svelte-p07anx">Base Rates</h4> <p class="svelte-p07anx">Hourly: €${escape_html(profile.base_hourly_rate.toFixed(2))}</p> <p class="svelte-p07anx">Minimum: €${escape_html(profile.minimum_charge.toFixed(2))}</p></div> <div class="rate-group svelte-p07anx"><h4 class="svelte-p07anx">Size Factors</h4> <p class="svelte-p07anx">Per m²: €${escape_html(profile.rate_per_m2.toFixed(2))}</p> <p class="svelte-p07anx">Per floor: €${escape_html(profile.rate_per_floor.toFixed(2))}</p></div> <div class="rate-group svelte-p07anx"><h4 class="svelte-p07anx">Room Factors</h4> <p class="svelte-p07anx">Per bedroom: €${escape_html(profile.rate_per_bedroom.toFixed(2))}</p> <p class="svelte-p07anx">Per bathroom: €${escape_html(profile.rate_per_bathroom.toFixed(2))}</p></div> <div class="rate-group svelte-p07anx"><h4 class="svelte-p07anx">Multipliers</h4> <p class="svelte-p07anx">Premium: ×${escape_html(profile.premium_multiplier)}</p> <p class="svelte-p07anx">Luxury: ×${escape_html(profile.luxury_multiplier)}</p></div></div> <div class="profile-surcharge svelte-p07anx"><strong>Dog surcharge:</strong> €${escape_html(profile.dog_surcharge.toFixed(2))}</div> <div class="profile-actions svelte-p07anx"><button class="btn btn-secondary btn-sm"${attr("title", tt("hints.edit_item"))}>${escape_html(tt("common.edit"))}</button> <button class="btn btn-secondary btn-sm" title="Copy profile">Copy</button> `);
        if (!profile.is_standard) {
          $$renderer2.push("<!--[0-->");
          $$renderer2.push(`<button class="btn btn-danger btn-sm"${attr("title", tt("hints.delete_item"))}>${escape_html(tt("common.delete"))}</button>`);
        } else {
          $$renderer2.push("<!--[-1-->");
        }
        $$renderer2.push(`<!--]--></div></div>`);
      }
      $$renderer2.push(`<!--]--></div>`);
    }
    $$renderer2.push(`<!--]--></div> `);
    {
      $$renderer2.push("<!--[-1-->");
    }
    $$renderer2.push(`<!--]-->`);
    if ($$store_subs) unsubscribe_stores($$store_subs);
  });
}
export {
  _page as default
};
