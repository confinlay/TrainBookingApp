import { h, Component } from "https://esm.sh/preact@10.19.2";
import htm from "https://esm.sh/htm@3.1.1";
import { getAuth } from "./state.js";

const html = htm.bind(h);

export class Trains extends Component {
  constructor() {
    super();
    this.state = {
      trains: [],
    };
  }

  async componentDidMount() {
    const response = await fetch("/api/getTrains", {
      headers: {
        Authorization: `Bearer ${await getAuth().currentUser.getIdToken(
          false,
        )}`,
      },
    });
    if (!response.ok) {
      return html`${await response.text()}`;
    }
    const trains = await response.json();

    this.setState({ trains });
  }

  render() {
    return html`
      <div class="page">
        <div>
          <h1>Trains</h1>
        </div>
        <div class="trains-grid">
          ${this.state.trains.map(
            (train) => html`
              <a href="/trains/${train.trainCompany}/${train.trainId}">
                <div class="trains-item">
                  <img async src="${train.image}" />
                  <div>
                    <div class="trains-item-name">${train.name}</div>
                    <div class="trains-item-location">${train.location}</div>
                  </div>
                </div>
              </a>
            `,
          )}
        </div>
      </div>
    `;
  }
}
