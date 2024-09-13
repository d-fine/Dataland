<template>
  <LegalPage>
    <div class="flex justify-content-start align-items-center">
      <img src="/public/static/icons/uk_flag.jpg" height="20" alt="English" />
      <InputSwitch
        data-test="terms-language-toggle-button"
        v-model="languageGerman"
        class="p-inputswitch-singlecolor"
        style="margin-left: 10px; margin-right: 10px"
      />
      <img src="/public/static/icons/germany_flag.png" height="20" alt="German" />
    </div>

    <div v-if="!languageGerman">
      <h1>General terms and conditions for participation in Dataland</h1>
      <h1>(„AGB-Dataland“)</h1>
      <div class="no-numbering">
        <h2>Preliminary remark</h2>
        <p>
          Dataland GmbH (hereinafter: "Dataland") operates a platform (hereinafter: "Dataland Platform") to compile
          company-related data, in particular in the areas of environment ("Environmental"), social affairs ("Social")
          and corporate governance ("Governance"), and to make it available to its users (hereinafter: "Data Users")
          (hereinafter: "Company Data").
        </p>
        <p>
          The (natural and legal) persons registered on the Dataland platform (hereinafter: "Dataland participants") can
          transfer Company Data to Dataland as so-called Data Collectors. Companies can do the same with the Company
          Data that concerns them (so-called "Company Owners"). The Company Data on the Dataland platform can be
          downloaded and used by Data Users.
        </p>
        <p>
          The aim of Dataland is to create a knowledge platform that is open to everyone and to make Company Data
          available to all Data Users. Dataland checks the entered Company Data with automated tests. In addition,
          random checks are carried out on individual data. Otherwise, the content of the Company Data transmitted by
          the Data Collectors or Company Owners to the Dataland platform and made available there is not checked.
        </p>
        <p>
          The following terms and conditions govern the general registration as a Dataland Participant as well as the
          registration and participation in Dataland as a Data User, Data Collector and Company Owner.
        </p>
      </div>
      <div>
        <h2>Registration on the Dataland platform</h2>
        <p>In order to participate in Dataland, it is first necessary to register as a Dataland Participant.</p>
        <p>
          In addition to registration as a Dataland Participant (but not without it), registrations as a Data Collector,
          Company Owner and Data User may be made.
        </p>
        <p>The prerequisite for registration of the (natural or legal) person is their full legal capacity.</p>
        <p>
          A (natural or legal) person may only register as one Dataland Participant and, based on this registration, may
          only register as one Data Collector, Company Owner and/or Data User.
        </p>
        <p>
          Registration with Dataland initially takes place in such a way that the mandatory fields marked with an "*"
          are filled in completely and truthfully. Additional information (marked as optional) can also be provided.
        </p>
        <p>
          The registrant can give him-/herself a login name and password during the registration process. The password
          must be kept secret and may not be made available to third parties. If a Dataland Participant becomes aware
          that third parties have learned its password or have gained unauthorised access to Dataland under its login
          name, Dataland must be informed of this immediately.
        </p>
        <p>
          After the mandatory fields have been completed and the corresponding application for registration has been
          sent by clicking on the "Request registration" field, Dataland will review the application for registration.
          After review and a positive decision on the application for registration, the applicant will be confirmed as a
          Dataland Participant or, in the case of corresponding applications, as a Data Collector, Company Owner or Data
          User.
        </p>
        <p>Dataland is under no obligation to accept and confirm a registration.</p>
        <p>
          Insofar as circumstances change after registration that the information provided there become incorrect, the
          Dataland Participant shall be obliged to inform Dataland thereof without undue delay, namely - to the extent
          possible - by adjusting the Dataland Participant's personal profile on the Dataland Platform.
        </p>
        <p>
          Irrespective of other cancellation rights, Dataland is entitled to cancel or revoke participation in Dataland
          and all registrations with immediate effect if the Dataland Participant has provided false information during
          registration, has registered twice or has breached any contractual obligations incumbent upon it.
        </p>
      </div>
      <div>
        <h2>Conditions applicable to Dataland Participants:</h2>
        <div>
          <h3>The rights of use of the Dataland Participant</h3>
          <p>
            Upon registration, the Dataland Participant receives the right to access the Dataland Platform. This
            includes, in particular, the right to view the Company Data on the website (read-only right). The Company
            Data may not be copied or downloaded.
          </p>
          <p>
            As a Dataland Participant, it has the option of registering as a Data User, Company Owner or Data Collector
            in accordance with the provisions of § 1.
          </p>
        </div>
        <div>
          <h3>The Dataland Participant's remuneration obligations</h3>
          <p>
            Registration as a Dataland Participant and the associated use of the Dataland platform as a Dataland
            Participant are free of charge, regardless of whether it is for private or commercial use.
          </p>
        </div>
        <div>
          <h3>Term of registration as a Dataland Participant</h3>
          <p>Registration as a Dataland Participant is for an indefinite term.</p>
          <p>
            The registration can be cancelled by either party with one month's notice to the end of a calendar month.
          </p>
          <p>The right to terminate for good cause remains unaffected in all cases.</p>
          <p>
            At the end of the registration as a Dataland Participant, all further registrations of the respective
            Dataland Participant shall also end automatically.
          </p>
        </div>
      </div>
      <div>
        <h2>The following additional conditions apply to Data Users:</h2>
        <div>
          <h3>The rights of use of the Data User</h3>
          <p>
            The Data User may use the Company Data obtained from Dataland for its business or private purposes in
            accordance with the following provisions. The Data User therefore receives a non-exclusive, simple,
            non-transferable, non-sublicensable right to use the Company Data, limited in time to the duration of the
            registration as a Data User.
          </p>
          <p>
            The reproduction, distribution or display to the public of the database consisting of the Company Data as a
            whole or of a substantial part of the database in terms of type and scope is not permitted. The repeated and
            systematic reproduction, distribution or public display of parts of the database that are insignificant in
            terms of type and scope shall be deemed equivalent to the reproduction, distribution or public communication
            of a part of the database that is significant in terms of type and scope, provided that these actions run
            counter to a normal evaluation of the database or unreasonably impair the legitimate interests of Dataland.
            In all other respects, §§ 87a ff UrhG shall apply.
          </p>
          <p>
            The search for Company Data and on the Dataland Platform may only be carried out using the access methods
            provided by Dataland (e.g. search functions, APIs). Bypassing this search function, e.g. through automated
            queries of the database and in general when using the Dataland Platform, is not permitted.
          </p>
          <p>
            When using the Company Data, the Data User must take into account the fact that the Company Data transmitted
            by the Data Collectors is provided and published as it is transmitted to Dataland. The Company Data is
            checked by Dataland prior to its initial provision and subsequently in terms of content only using automated
            tests. In addition, spot checks are carried out on individual data. Otherwise, the Company Data is not
            checked, verified or monitored with regard to its up-to-dateness and only errors that become positively
            known are corrected within the scope of what is possible and economically justifiable. For this reason, no
            guarantee can be given that the Company Data is correct, up to date and complete at a given time. Therefore,
            before making decisions of economic relevance, including investment decisions, on the basis of the Company
            Data made available on the Dataland platform, the Data User is obliged to check the accuracy and
            completeness of the content as well as verify that it is up to date. The Company Data and the information
            obtained from Dataland may only be used as a basis for investment decisions if this obligation is met.
          </p>
          <p>
            The Data User has no right to access the Dataland Platform at certain times and to access Company Data at
            certain times. In particular, Dataland shall not be obliged to notify the Data User of any periods of
            unavailability of the Platform Dataland or to guarantee unlimited and uninterrupted availability of the
            Platform Dataland in any other way.
          </p>
          <p>
            The Data User has no claim to the availability of certain Company Data on the Platform-Dataland. This also
            applies in particular to Company Data that was previously available there.
          </p>
        </div>
        <div>
          <h3>The Data User's remuneration obligations</h3>
          <p>
            For private individuals, NGOs and non-profit research or teaching organisations (collectively also referred
            to as "Private Use" in these GTC-Dataland ), the use of Dataland is free of charge.
          </p>
          <p>
            A monthly flat rate is payable for use by all other Data Users, which is based on Dataland's
            <a href="/pricing">current price scale</a>.
          </p>
          <p>All fees are exclusive of the applicable value added tax.</p>
          <p>The remuneration shall be paid monthly in advance by the third working day of each month at the latest.</p>
          <p>
            The Data User may fulfil his payment obligations not only by bank transfer but also by any of the payment
            methods indicated as payment methods on the Platform-Dataland.
          </p>
        </div>
        <div>
          <h3>Term of registration as a Data User</h3>
          <p>
            During the registration process, the Data User may choose to register as a Data User for a specific term or
            for an indefinite term.
          </p>
          <p>
            In the case of an indefinite term, either party may terminate the registration with one month's notice to
            the end of a calendar month. In the case of a fixed term, cancellation is not possible until the end of the
            term.
          </p>
          <p>
            Insofar as the registration as a Dataland Participant is cancelled before the expiry of an agreed specific
            term, the registration as a Data User shall also end as a result. If the registration as a Dataland
            subscriber is cancelled due to a termination by Dataland that was not pronounced for good cause, Dataland
            shall be obliged to reimburse the Data User pro rata remuneration for the period that had not yet expired at
            the time of the termination of the registration as a Data User.
          </p>
          <p>The right to terminate the contract for good cause remains unaffected in all cases.</p>
        </div>
        <div>
          <h3>Liability of Dataland towards Data Users</h3>
          <p>
            Dataland shall be liable to the Data User only in accordance with the following provisions and only to the
            extent that the Company Data is used and utilised by the Data User within the scope of the rights of use
            granted to the Data User and in compliance with the Data User's own contractual obligations.
          </p>
          <p>
            Dataland shall be liable for damages caused by slight negligence if the act of infringement attributable to
            Dataland constitutes a breach of cardinal obligations, whereby cardinal obligations are the essential
            obligations which form the basis of the use of the Platform-Dataland and the Company Data, which were
            decisive for the use of the Platform-Dataland and the Company Data and on the fulfilment of which the Data
            User may rely.
          </p>
          <p>
            If Dataland has breached cardinal obligations only through slight negligence, Dataland's liability shall be
            limited to foreseeable damage.
          </p>
          <p>
            Dataland shall also only be liable for the loss of data up to the amount that would have been incurred if
            the data had been properly and regularly backed up in order to restore it.
          </p>
          <p>
            In any event, i.e. notwithstanding any other provisions in these GTC-Dataland shall be liable without
            limitation for damage caused intentionally or by gross negligence as well as for damage resulting from
            injury to life, body or health by Dataland, its legal representatives or vicarious agents.
          </p>
          <p>
            Dataland shall be liable for product liability damages in accordance with the provisions of the Product
            Liability Act.
          </p>
        </div>
      </div>
      <div>
        <h2>The following additional conditions apply to Data Collectors:</h2>
        <div>
          <h3>Transfer of data to Dataland by the Data Collector</h3>
          <p>
            The Data Collector collates Company Data about a data object. It inserts the Company Data in relation to a
            data object into the data matrix that Dataland holds on the Dataland platform for this purpose.
          </p>
          <p>
            The Company Data is provided by the Data Collector in relation to individual company topics or questions.
            Such a single unit is referred to as a data point. Several data points form a data set.
          </p>
          <p>
            The Data Collector can transfer Company Data to Dataland for a data object for which there was previously no
            Company Data. He can also supplement existing data records for a data object with missing data or update
            them with more recent Company Data.
          </p>
          <p>
            Dataland shall have the right to review the Company Data entered by the Data Collector in accordance with
            Dataland's own requirements and at Dataland's sole discretion. Based on this review, Dataland shall decide
            whether the Company Data provided by the Data Collector shall be authorised as Company Data of the Dataland
            Platform for access by Data Users.
          </p>
          <p>
            Company Data authorised in this way shall be made available to Data Users on the Platform Data Land. As long
            as they are available to Data Users on the Platform Data Land, the Company Data shall serve as the basis for
            calculating the Data Collector's claim to remuneration.
          </p>
          <p>
            Dataland shall inform the Data Collector if Company Data is not authorised. Dataland is not obliged to state
            the reasons for the refused authorisation. There is no entitlement to remuneration for Company Data that is
            not authorised or subsequently removed.
          </p>
          <p>
            Even after the Company Data has been admitted to the Dataland Platform, Dataland shall have the right at any
            time and without restriction to review the Company Data in accordance with its own requirements and at
            Dataland's sole discretion and to decide on its retention on the Dataland Platform. In this respect,
            Dataland may freely decide at any time whether this Company Data provided by the Data Collector is to remain
            on the Dataland Platform as Company Data for access by Data Users or whether access to it is to be blocked.
            In the event of a blocking or removal of the Company Data, § 4.1.6 shall apply accordingly.
          </p>
          <p>
            The Data Collector has no right to access the Platform Dataland at certain times and to transmit Company
            Data from the Platform Dataland at certain times. In particular, Dataland shall not be obliged to notify the
            Data Collector of any periods of unavailability of the Platform Dataland or to otherwise guarantee unlimited
            and uninterrupted availability of the Platform Dataland.
          </p>
          <p>
            The Data Collector is aware that Dataland is entitled to make the Company Data available to the Data Users
            and Company Owners free of charge during the ramp-up phase of Dataland until spring 2024 and that this has a
            direct effect on the Data Collector's entitlement to remuneration pursuant to section 4.4.
          </p>
        </div>
        <div>
          <h3>Granting of rights of use to Dataland by the Data Collector</h3>
          <p>
            By submitting the Company Data on the Dataland Platform, the Data Collector transfers to Dataland a right of
            use in such a way that Dataland receives an irrevocable, spatially and content-wise unlimited, perpetual,
            transferable and sub-licensable right of use to all data as well as to the entirety of the data, even if
            these should form a database of § 87a of the Copyright Act. This includes in particular the non-withdrawable
            and non-revocable right to use this Company Data in any conceivable manner, in particular also in the
            context of any business activity, and to make it available or transfer it to third parties, so that these
            third parties also have corresponding rights. Likewise, Dataland has the unrestricted right to modify and/or
            process individual Company Data, the data in its entirety as well as any databases as a whole.
          </p>
          <p>
            In the event that the provisions in this contract are not sufficient to transfer the corresponding rights to
            Dataland to this extent, the Data Collector is obliged to bring about and effect such a granting of rights
            through corresponding separate legal declarations and actual acts.
          </p>
          <p>
            The Data Collector guarantees to transfer only such Company Data to Dataland that are free of third party
            rights and for which neither contractual nor legal requirements prevent an actual transfer pursuant to § 4.1
            or the legally effective transfer of rights pursuant to this § 4.2.
          </p>
        </div>
        <div>
          <h3>Duties of the Data Collector</h3>
          <p>
            The Data Collector is obliged to transmit only such Company Data to Dataland of which the Data Collector is
            convinced, to the best of its knowledge and belief and applying the care required in business transactions,
            that this Company Data corresponds to actual circumstances and facts. In particular, the Company Data must
            not make a statement that distorts the meaning by omitting circumstances or facts.
          </p>
          <p>
            The Data Collector is obliged to transfer to Dataland only such Company Data that Dataland can make
            available to the Data Users for the use described therein on the Dataland Platform without restriction and
            in particular without obtaining any consent or authorisation to that effect or without having to rely on
            statutory permissions in accordance with § 3.1, also with regard to the data protection laws and legal
            requirements.
          </p>
        </div>
        <div>
          <h3>The Data Collector's claims to remuneration</h3>
          <p>
            The starting point for the Data Collector's remuneration is the Company Data that the Data Collector has
            made available to Dataland in relation to the individual company topics and that Dataland has consequently
            included in the Dataland Platform and that is available to the Data Users. The actual retrieval of the
            corresponding information by Data Users is not relevant with regard to the remuneration of the Data
            Collector.
          </p>
          <p>
            Based on this principle, the remuneration of the Data Collector for the respective calendar year just ended
            is calculated as follows:
          </p>
          <ul>
            <li>
              After the end of each calendar year, Dataland shall determine a share of the (net) revenue generated in
              accordance with § 3.2 (not taking into account the remuneration paid by means of Dataland Tokens), which
              shall be available as a total amount to remunerate the Data Collectors, whereby there is a lower limit for
              this share which can be viewed in the <a href="/pricing">current price and remuneration schedule</a>.
            </li>
            <li>
              Furthermore, Dataland determines the distribution amount available for the respective day based on the
              revenue actually generated for each calendar day of this past calendar year ("Claim Days").
            </li>
            <li>
              Finally, Dataland determines how many data points according to § 4.1.2 existed in total on each individual
              day and how many data points are to be allocated to each individual Data Collector. In the case of data
              points for which there are versions of differing timeliness, only the most recent version of the data
              point is taken into account. The Data Collector is entitled to the percentage share determined for it.
            </li>
            <li>
              Remuneration claims below the technical minimum amount will not be paid out. The currently applicable
              technical minimum amount is indicated in the
              <a href="/pricing">current price and remuneration schedule</a>.
            </li>
          </ul>
          <p>
            Even after the end of registration as a Data Collector, the Data Collector shall be entitled to remuneration
            in accordance with the above provisions for the Company Data that continues to be available on the
            Platform-Dataland.
          </p>
          <p>
            All calculation bases on which the calculation of the Data Collector's remuneration claim is based in
            accordance with the above shall be made available to the Data Collector so that the Data Collector can
            understand and verify the calculation of its remuneration claim.
          </p>
          <p>
            Dataland shall pay the remuneration to the Data Collector within three months after the end of a calendar
            year for the Data Collector's services subject to remuneration during that calendar year.
          </p>
        </div>
        <div>
          <h3>Term of registration as a Data Collector</h3>
          <p>Registration as a Data Collector is for an indefinite term.</p>
          <p>
            The registration can be cancelled by either party with one month's notice to the end of a calendar month.
          </p>
          <p>The right to terminate the contract for good cause remains unaffected in all cases.</p>
        </div>
      </div>
      <div>
        <h2>The following additional conditions apply to Company Owners:</h2>
        <p>
          Company Owners may also register as such on Dataland. As Company Owners, they have the right to use the
          Dataland Platform in the same way as a Data Collector or a Data User.
        </p>
        <p>
          Insofar as the Company Owner acts in accordance with a Data User, the provisions of § 3 shall apply, including
          the remuneration rules therein.
        </p>
        <p>
          Insofar as the Company Owner acts as a Data Collector, the provisions of § 4 shall apply, including the
          remuneration rules therein.
        </p>
        <div>
          <h3>Remuneration and payments</h3>
          <p>
            Registration as a Company Owner and the associated use of the Dataland platform as a Company Owner are free
            of charge, regardless of whether it is for private or commercial use.
          </p>
          <p>
            Irrespective of the remuneration to which a Company Owner is entitled due to an activity as a Data Collector
            pursuant to § 5.3 in conjunction with § 4, he/she may receive (additional) remuneration in accordance with
            the following principles:
          </p>
          <p>
            The starting point for the possible additional remuneration of the Company Owner is the data points that
            relate to the respective Company Owner during a calendar year.
          </p>
          <p>
            Based on this principle, a possible additional remuneration of the Company Owner after the end of the
            calendar year is derived as follows for this calendar year:
          </p>
          <ul>
            <li>
              After the end of each calendar year, Dataland may determine a share of the (net) income generated in
              accordance with § 3.2 (not taking into account the remuneration paid by means of Dataland Tokens), which
              shall be available as a total amount for the separate remuneration of the Company Owners.
            </li>
            <li>
              Furthermore, Dataland determines the distribution amount available for the respective day based on the
              revenue actually generated for each calendar day of this past calendar year ("Claim Days").
            </li>
            <li>
              Finally, Dataland determines how many data points according to § 4.1.2 existed in total on each individual
              day and how many data points are to be allocated to each individual company owner. In the case of data
              points for which there are versions of differing timeliness, only the most recent version of the data
              point is taken into account. A company owner is entitled to the percentage share determined for it
            </li>
            <li>
              Remuneration claims below the technical minimum amount will not be paid out. The currently applicable
              technical minimum amount is stated in the <a href="/pricing">current price and remuneration schedule</a>.
            </li>
          </ul>
          <p>
            All calculation bases on which the calculation of the Company Owner's remuneration claim is based in
            accordance with the above paragraph shall be made available to the Company Owner so that the Company Owner
            can understand and verify the calculation of its remuneration claim.
          </p>
          <p>
            Dataland shall pay the remuneration to the Company Owner within three months after the end of a calendar
            year for the Company Owner's services subject to remuneration during that calendar year.
          </p>
        </div>
        <div>
          <h3>Term of registration as a company owner</h3>
          <p>Registration as a company owner is for an indefinite term.</p>
          <p>
            The registration can be cancelled by either party with one month's notice to the end of a calendar month.
          </p>
          <p>Cancellation for good cause remains unaffected in any case.</p>
        </div>
      </div>
      <div>
        <h2>Dataland token</h2>
        <p>
          In addition to the unrestricted possibility of fulfilling all payment obligations in connection with the
          Dataland Platform by making payments on a euro basis, it is also possible to fulfil payment obligations using
          these Dataland Tokens by means of the Dataland Tokens created and issued by Dataland. The Dataland Tokens are
          issued and used in accordance with the following principles:
        </p>
        <p>Dataland will create Dataland tokens. Each Dataland token has a nominal value of one euro.</p>
        <p>
          The prerequisite for the possession and use of Dataland Tokens by Dataland Participants is the conclusion of a
          <a href="/token">Token Custody Agreement</a> between the Dataland Participant and the Custodian.
        </p>
        <p>
          Dataland undertakes to accept Dataland Tokens as a means of payment in accordance with the provisions of these
          GTC-Dataland in such a way that one Dataland Token is equivalent to one euro.
        </p>
        <p>Dataland is not obliged to take back Dataland Tokens or to refund payments made for Dataland Tokens.</p>
        <p>
          Dataland is free and in no way restricted with regard to the use and transfer of Dataland Tokens. In
          particular, Dataland Participants are not entitled to any rights to demand specific behaviour from Dataland in
          relation to the Dataland Tokens, unless Dataland has committed itself to specific behaviour in relation to the
          Dataland Tokens by virtue of express contractual provisions.
        </p>
        <div>
          <h3>Dataland intends to offer and transfer the Dataland Tokens to Dataland Participants as follows:</h3>
          <p>
            Dataland intends to offer Company Owners the purchase of Dataland Tokens. In doing so, Dataland does not act
            at its own discretion with regard to the number of Dataland Tokens offered in each case, nor with regard to
            their pricing, and in particular without any obligation to treat Company Owners equally.
          </p>
          <p>
            Dataland intends to offer Data Collectors the fulfilment of their remuneration claims with Dataland Tokens.
            In doing so, Dataland acts at its own discretion both with regard to the number of Dataland Tokens offered
            in each case and with regard to their pricing, and in particular without any obligation to treat Data
            Collectors equally. Data Collectors are free to decide whether they wish to accept a corresponding offer.
          </p>
          <p>
            Dataland may also offer the purchase of Dataland Tokens to the other Dataland Participants. Here too,
            Dataland shall act at its own discretion both with regard to the number of Dataland Tokens offered in each
            case and with regard to their pricing and, in particular, without any obligation to treat Dataland
            Participants equally in this respect.
          </p>
        </div>
        <p>
          As soon as Dataland Tokens are available for use and purchase on Dataland, Dataland Participants have the
          right to fulfil 50% of their respective payment obligations to Dataland with Dataland Tokens.
        </p>
      </div>
      <div>
        <h2>Data protection</h2>
        <p>
          Dataland collects, processes and uses personal data of Dataland participants in strict compliance with the
          applicable laws, in particular the GDPR and the Federal Data Protection Act. Further information can be found
          in our <a href="/dataprivacy">privacy policy</a>.
        </p>
      </div>
      <div>
        <h2>Content and changes to the GTC-Dataland</h2>
        <p>
          Amendments or supplements to these GTC-Dataland as well as other agreements between Dataland and the user,
          which are subject to the provisions of the GTC, must be made in writing.
        </p>
        <p>
          Should any provision of these GTC-Dataland be or become invalid, this shall not affect the validity of the
          remaining provisions of these GTC. Dataland and the user hereby undertake to agree on a new effective
          provision that comes as close as possible to the purpose of the invalid provision. The same shall apply to
          contractual loopholes.
        </p>
        <p>
          Dataland may update or amend these GTC-Dataland by offering the amended contractual documents to the Dataland
          Participant for acceptance and requesting acceptance.
        </p>
        <p>
          If the Dataland Participant does not accept the amended contract documents within 8 weeks, Dataland shall have
          the right to terminate all registrations in the name of the Dataland Participant with immediate effect by
          cancellation.
        </p>
      </div>
      <div>
        <h2>Final provisions</h2>
        <p>
          The registered user may only transfer or assign rights, claims and legal positions arising from this contract
          to third parties with the written consent of Dataland.
        </p>
        <p>
          The business relationship between Dataland and the user governed by these GTC-Dataland is subject to the laws
          of the Federal Republic of Germany, excluding the UN Convention on Contracts for the International Sale of
          Goods ("C.I.S.G").
        </p>
        <p>
          If the Dataland User is a merchant or has no general place of jurisdiction in Germany, the place of fulfilment
          and exclusive place of jurisdiction for all legal disputes arising from or in connection with the business
          relationship between Dataland and the Dataland User governed by these GTC-Dataland shall be Frankfurt am Main
          (Germany).
        </p>
      </div>
      <div>
        <h2>Priority of the German version</h2>
        <p>
          The German version of this GTC-Dataland is authoritative. The English version is for information purposes only
          for the benefit of non-German speaking persons.
        </p>
      </div>
      <div class="w-12 text-center font-bold mt-6">- End of AGB-Dataland -</div>
    </div>
    <div v-if="languageGerman">
      <h1>Allgemeine Bedingungen für die Teilnahme an Dataland</h1>
      <h1>(„AGB-Dataland“)</h1>
      <div class="no-numbering">
        <h2>Vorbemerkung</h2>
        <p>
          Die Dataland GmbH (nachfolgend: „Dataland“) betreibt eine Plattform (nachfolgend: „Plattform-Dataland“, um
          unternehmensbezogene Daten, insbesondere in den Bereichen Umwelt („Environmental“), Soziales („Social“) und
          Unternehmensführung („Governance“) zusammenzutragen und für ihre Nutzer (nachfolgend: „Daten-User“) verfügbar
          zu machen (nachfolgend „Unternehmens-Daten“).
        </p>
        <p>
          Die auf der Plattform-Dataland registrierten (natürlichen und juristischen) Personen (nachfolgend:
          „Dataland-Teilnehmer“) können als sogenannte Daten-Sammler Unternehmens-Daten an Dataland übertragen. Das
          Gleiche können Unternehmen mit den Unternehmens-Daten tun, die sie selbst betreffen (sog. „Daten-Owner“). Die
          Unternehmens-Daten auf der Plattform-Dataland können von den Daten-Usern heruntergeladen und genutzt werden.
        </p>
        <p>
          Ziel von Dataland ist es, eine allen offenstehende Wissensplattform zu schaffen, und die Unternehmens-Daten
          allen Daten-Usern zur Nutzung zur Verfügung zu stellen. Dataland prüft die eingegebenen Unternehmens-Daten mit
          automatisierten Tests. Darüber hinaus werden Stichproben mit Einzeldaten durchgeführt. Ansonsten erfolgt keine
          inhaltliche Überprüfung der von den Daten-Sammlern oder den Daten-Ownern an die Plattform-Dataland
          übermittelten und dort zur Verfügung gestellten Unternehmens-Daten.
        </p>
        <p>
          Die folgenden Bedingungen regeln die allgemeine Registrierung als Dataland-Teilnehmer sowie die Registrierung
          und Teilnahme an Dataland als Daten-User, als Daten-Sammler und als Daten-Owner.
        </p>
      </div>
      <div>
        <h2>Die Registrierung auf der Plattform-Dataland</h2>
        <p>
          Für die Teilnahme an Dataland ist es zunächst erforderlich, dass eine Registrierung als Dataland-Teilnehmer
          erfolgt.
        </p>
        <p>
          Zusätzlich zu der Registrierung als Dataland-Teilnehmer (jedoch nicht ohne diese) können Registrierungen als
          Daten-Sammler, Daten-Owner und Daten-User erfolgen.
        </p>
        <p>
          Voraussetzung für die Registrierung der (natürlichen oder juristischen) Person ist deren volle
          Geschäftsfähigkeit.
        </p>
        <p>
          Eine (natürliche oder juristische) Person darf sich nur als ein Dataland-Teilnehmer registrieren und basierend
          auf dieser Registrierung jeweils nur eine Registrierung als Daten-Sammler, Daten-Owner und/oder Daten-User
          vornehmen.
        </p>
        <p>
          Eine Registrierung bei Dataland erfolgt zunächst in der Weise, dass die mit einem „*“ versehenen Pflichtfelder
          vollständig und wahrheitsgemäß ausgefüllt werden. Zusätzlich können weitere (als optional gekennzeichnete)
          Angaben gemacht werden.
        </p>
        <p>
          Der Registrierende kann sich hierbei im Zuge der Registrierung selbst einen Login-Namen und ein Passwort
          geben. Das Passwort ist geheim zu halten und darf nicht Dritten zur Verfügung gestellt werden. Soweit ein
          Dataland-Teilnehmer davon Kenntnis erlangt, dass Dritte sein Passwort erfahren haben oder sich unberechtigt
          unter dessen Login-Namen Zugang zu Dataland verschafft haben, ist Dataland unverzüglich hierüber zu
          informieren.
        </p>
        <p>
          Nachdem die Pflichtfelder ausgefüllt und der entsprechende Antrag auf Registrierung durch Anklicken des Feldes
          „Registrierung anfragen“ abgesendet wurde, überprüft Dataland den Antrag auf Registrierung. Nach erfolgter
          Überprüfung und positiver Entscheidung über den Antrag auf Registrierung wird dem Antragsteller die
          Registrierung als Dataland-Teilnehmer bzw. bei entsprechenden Anträgen als Daten-Sammler, Daten-Owner oder
          Daten-User bestätigt.
        </p>
        <p>Seitens Dataland besteht keine Pflicht, eine Registrierung zu akzeptieren und diese zu bestätigen.</p>
        <p>
          Soweit sich nach einer Registrierung Umstände ändern, die die dort gemachten Angaben unrichtig werden lassen,
          ist der Dataland-Teilnehmer verpflichtet, Dataland hierüber unverzüglich zu unterrichten, und zwar – soweit
          möglich – durch Anpassung des persönlichen Profils des Dataland-Teilnehmers auf der Plattform-Dataland.
        </p>
        <p>
          Unabhängig von sonstigen Kündigungsrechten ist Dataland berechtigt, die Teilnahme an Dataland und alle
          Registrierungen mit sofortiger Wirkung zu kündigen oder zu widerrufen, wenn der Dataland-Teilnehmer bei der
          Registrierung falsche Angaben gemacht hat, sich doppelt registriert hat oder gegen ihm obliegende
          Vertragspflichten verstoßen hat.
        </p>
      </div>
      <div>
        <h2>Für Dataland-Teilnehmer geltende Bedingungen:</h2>
        <div>
          <h3>Die Nutzungsrechte des Dataland-Teilnehmers</h3>
          <p>
            Der Dataland-Teilnehmer erhält mit seiner Registrierung das Zugriffsrecht auf die Plattform-Dataland. Dies
            umfasst insbesondere die reine Ansicht der Unternehmens-Daten auf der Website (reines Leserecht).
          </p>
          <p>
            Als Dataland-Teilnehmer hat er die Möglichkeit gemäß den Maßgaben in § 1 Registrierungen als Daten-User,
            Daten-Owner oder Daten-Sammler vorzunehmen.
          </p>
        </div>
        <div>
          <h3>Die Vergütungspflichten des Dataland-Teilnehmers</h3>
          <p>
            Die Registrierung als Dataland-Teilnehmer und die damit verbundene Nutzung der Plattform-Dataland als
            Dataland-Teilnehmer sind kostenlos, unabhängig davon, ob es sich um eine Privatnutzung oder um eine
            gewerbliche Nutzung handelt.
          </p>
        </div>
        <div>
          <h3>Laufzeit der Registrierung als Dataland-Teilnehmer</h3>
          <p>Die Registrierung als Dataland-Teilnehmer erfolgt für eine unbestimmte Laufzeit.</p>
          <p>
            Die Registrierung kann von jeder Partei mit einer Frist von einem Monat zum Ende eines Kalendermonats
            gekündigt werden.
          </p>
          <p>Das Recht zur Kündigung aus wichtigem Grund bleibt in jedem Fall unberührt.</p>
          <p>
            Mit dem Ende der Registrierung als Dataland-Teilnehmer enden automatisch auch alle weiteren Registrierungen
            des jeweiligen Dataland-Teilnehmers.
          </p>
        </div>
      </div>
      <div>
        <h2>Für Daten-User gelten die folgenden zusätzlichen Bedingungen:</h2>
        <div>
          <h3>Die Nutzungsrechte des Daten-Users</h3>
          <p>
            Der Daten-User darf die aus Dataland bezogenen Unternehmens-Daten für seine geschäftlichen oder privaten
            Zwecke nach nachfolgenden Maßgaben nutzen. Der Daten-User erhält daher an diesen ein nicht-ausschließliches,
            einfaches, nicht übertragbares, nicht unterlizenzierbares zeitlich auf die Dauer der Registrierung als
            Daten-User beschränktes Recht zur Nutzung der Unternehmens-Daten.
          </p>
          <p>
            Die Vervielfältigung, Verbreitung oder öffentliche Wiedergabe der Datenbank, die aus den Unternehmens-Daten
            besteht, insgesamt oder eines nach Art und Umfang wesentlichen Teils der Datenbank ist nicht gestattet. Der
            Vervielfältigung, Verbreitung oder öffentlichen Wiedergabe eines nach Art oder Umfang wesentlichen Teils der
            Datenbank steht die wiederholte und systematische Vervielfältigung, Verbreitung oder öffentliche Wiedergabe
            von nach Art und Umfang unwesentlichen Teilen der Datenbank gleich, sofern diese Handlungen einer normalen
            Auswertung der Datenbank zuwiderlaufen oder die berechtigten Interessen von Dataland unzumutbar
            beeinträchtigen. Im Übrigen gelten die §§ 87a ff UrhG.
          </p>
          <p>
            Die Suche nach Unternehmens-Daten und auf der Plattform-Dataland darf nur mit den von Dataland zur Verfügung
            gestellten Zugangsmethoden (z.B. Suchfunktionen, APIs) erfolgen. Eine Umgehung dieser Suchfunktion z.B.
            durch automatisierte Abfragen der Datenbank und generell im Rahmen der Nutzung der Plattform-Dataland ist
            nicht gestattet.
          </p>
          <p>
            Bei der Nutzung der Unternehmens-Daten hat der Daten-User dem Umstand Rechnung zu tragen, dass die von den
            Daten-Sammlern übermittelten Unternehmens-Daten so wie sie übermittelt werden zur Verfügung gestellt und
            veröffentlicht werden. Die Unternehmensdaten werden von Dataland vor ihrer erstmaligen Bereitstellung und
            nachfolgend inhaltlich ausschließlich mit automatisierten Tests überprüft. Darüber hinaus werden Stichproben
            mit Einzeldaten durchgeführt. Ansonsten werden die Unternehmensdaten nicht überprüft, verifiziert oder
            hinsichtlich ihrer Aktualität überwacht und nur positiv bekannt gewordene Fehler werden im Rahmen des
            Möglichen und des wirtschaftlich Vertretbaren berichtigt. Aus diesem Grund kann keine Garantie dafür
            übernommen werden, dass die Unternehmensdaten zu einem bestimmten Zeitpunkt richtig, aktuell und vollständig
            sind. Deshalb ist der Daten-User jedenfalls dann, bevor er auf Basis der auf der Plattform-Dataland zur
            Verfügung gestellten Unternehmens-Daten Entscheidungen von wirtschaftlicher Relevanz, einschließlich
            Investitionsentscheidungen, trifft, verpflichtet, diese selbst auf ihre inhaltliche Richtigkeit und
            Vollständigkeit zu überprüfen und ihre Aktualität zu verifizieren. Die Unternehmens-Daten und die daraus
            gewonnenen Informationen dürfen nur dann als Grundlage für Investitionsentscheidungen genutzt werden, wenn
            dieser Verpflichtung entsprochen wurde.
          </p>
          <p>
            Der Daten-User hat keinen Anspruch darauf, zu bestimmten Zeiten auf die Plattform-Dataland zuzugreifen und
            zu bestimmten Zeiten Zugriff auf Unternehmens-Daten zu nehmen. Insbesondere ist Dataland nicht verpflichtet,
            dem Daten-User etwaige Zeiten der Nichtverfügbarkeit der Plattform-Dataland mitzuteilen oder in sonstiger
            Weise eine unbeschränkte und ununterbrochene Verfügbarkeit der Plattform-Dataland zu gewährleisten.
          </p>
          <p>
            Der Daten-User hat keinen Anspruch auf die Verfügbarkeit bestimmter Unternehmens-Daten auf der
            Plattform-Dataland. Dies gilt insbesondere auch für Unternehmens-Daten, die vormals dort verfügbar gewesen
            sind.
          </p>
        </div>
        <div>
          <h3>Die Vergütungspflichten des Daten-Users</h3>
          <p>
            Für Privatpersonen, NGOs und nicht gewinnorientiert tätige Forschungs- oder Lehreintrichtungen (insg. Im
            Rahmen dieser AGB-Dataland auch als „Privatnutzung“ bezeichnet) ist die Nutzung von Dataland kostenlos.
          </p>
          <p>
            Für die Nutzung durch alle anderen Daten-User ist eine monatliche Pauschale zu entrichten, die sich aus der
            jeweils <a href="/pricing">aktuellen Preisstaffel</a> von Dataland ergibt.
          </p>
          <p>Sämtliche Vergütungen verstehen sich zuzüglich der anwendbaren Umsatzsteuer.</p>
          <p>
            Die Vergütung ist jeweils monatlich im Voraus bis spätestens zum dritten Werktag eines Monats zu entrichten.
          </p>
          <p>
            Der Daten-User kann seine Zahlungspflichten außer mit Überweisung auch mittels all der Zahlungsmethoden
            bewirken, die auf der Plattform-Dataland als Zahlungsmethoden angegeben sind.
          </p>
        </div>
        <div>
          <h3>Laufzeit der Registrierung als Daten-User</h3>
          <p>
            Nach seiner Wahl kann sich der Daten-User im Zuge der Registrierung für eine bestimmte Laufzeit oder für
            eine unbestimmte Laufzeit der Registrierung als Daten-User entscheiden.
          </p>
          <p>
            Bei unbestimmter Laufzeit kann jede Partei die Registrierung mit einer Frist von einem Monat zum Ende eines
            Kalendermonats kündigen. Im Falle einer bestimmten Laufzeit ist die Kündigung bis zum Ablauf derselben nicht
            möglich.
          </p>
          <p>
            Soweit bei einer vereinbarten bestimmten Laufzeit vor deren Ablauf die Registrierung als
            Dataland-Teilnehmer, wegfällt, so endet hierdurch auch die Registrierung als Daten-User. Wenn die
            Registrierung als Dataland-Teilnehmer aufgrund einer Kündigung seitens Dataland wegfällt, die nicht wegen
            eines wichtigen Grundes ausgesprochen wurde, ist Dataland verpflichtet, dem Daten-User anteilige Vergütung
            für den Zeitraum zu erstatten, der zum Zeitpunkt der Beendigung der Registrierung als Daten-User noch nicht
            abgelaufen war.
          </p>
          <p>Das Recht zur Kündigung aus wichtigem Grund bleibt in jedem Fall unberührt.</p>
        </div>
        <div>
          <h3>Haftung von Dataland gegenüber Daten-Usern</h3>
          <p>
            Dataland haftet gegenüber dem Daten-User nur nach Maßgabe der folgenden Bestimmungen und nur soweit die
            Unternehmens-Daten von dem Daten-User im Rahmen der ihm eingeräumten Nutzungsrechte unter Befolgung der dem
            Daten-User obliegenden eigenen Vertragspflichten genutzt und eingesetzt werden.
          </p>
          <p>
            Dataland haftet für leicht fahrlässig verursachte Schäden dann, wenn die Dataland zuzurechnende
            Verletzungshandlung eine Verletzung von Kardinalpflichten darstellt, wobei es sich bei Kardinalpflichten um
            die wesentlichen Pflichten handelt, die die Grundlage der Nutzung der Plattform-Dataland und der
            Unternehmens-Daten bilden, die entscheidend für die Nutzung der Plattform-Dataland und der
            Unternehmens-Daten waren und auf deren Erfüllung der Daten-User vertrauen darf.
          </p>
          <p>
            Sollte Dataland Kardinalpflichten nur leicht fahrlässig verletzt haben, ist die Haftung von Dataland auf
            vorhersehbaren Schaden begrenzt.
          </p>
          <p>
            Dataland haftet zudem für den Verlust von Daten jedenfalls nur bis zu dem Betrag, der bei ordnungsgemäßer
            und regelmäßiger Sicherung der Daten zu deren Wiederherstellung angefallen wäre.
          </p>
          <p>
            Dataland haftet jedenfalls, d.h. unbeschadet anderer Regelungen in diesen AGB-Dataland, unbeschränkt für
            vorsätzlich oder grob fahrlässig verursachte Schäden sowie für Schäden aus der Verletzung des Lebens, des
            Körpers oder der Gesundheit durch Dataland, seine gesetzlichen Vertreter oder Erfüllungsgehilfen.
          </p>
          <p>Dataland haftet für Produkthaftungsschäden entsprechend der Regelungen des Produkthaftungsgesetzes.</p>
        </div>
      </div>
      <div>
        <h2>Für Daten-Sammler gelten die folgenden zusätzlichen Bedingungen:</h2>
        <div>
          <h3>Übertragung der Daten an Dataland durch den Daten-Sammler</h3>
          <p>
            Der Daten-Sammler trägt Unternehmens-Daten über ein Datenobjekt zusammen. Er fügt die Unternehmens-Daten in
            Bezug auf ein Datenobjekt in die Datenmatrix ein, die Dataland auf der Plattform-Dataland zu diesem Zwecke
            vorhält.
          </p>
          <p>
            Die Unternehmens-Daten werden vom Daten-Sammler in Bezug auf einzelne Unternehmens-Thematiken bzw. -Fragen
            zur Verfügung gestellt. Eine solche einzelne Einheit wird als Datenpunkt bezeichnet. Mehrere Datenpunkte
            bilden einen Datensatz.
          </p>
          <p>
            Der Daten-Sammler kann Unternehmens-Daten für ein Datenobjekt an Dataland übertragen, zu dem es bislang
            keine Unternehmens-Daten gab. Er kann auch bestehende Datensätze für ein Datenobjekt mit noch fehlenden
            Daten ergänzen oder durch jüngere Unternehmens-Daten aktualisieren.
          </p>
          <p>
            Dataland hat das Recht, die von dem Daten-Sammler eingegebenen Unternehmens-Daten nach eigenen und im
            ausschließlich eigenen Ermessen von Dataland liegenden Maßgaben zu überprüfen. Auf Grundlage dieser Prüfung
            entscheidet Dataland, ob die vom Daten-Sammler gelieferten Unternehmens-Daten als Unternehmens-Daten der
            Plattform-Dataland für den Zugriff von Daten-User zugelassen werden.
          </p>
          <p>
            Auf diese Weise zugelassene Unternehmens-Daten werden auf der Plattform-Dataland für Daten-User
            bereitgestellt. Solange sie für Daten-User auf der Plattform-Dataland zur Verfügung stehen, dienen die
            Unternehmens-Daten als Berechnungsgrundlage für den Vergütungsanspruch des Daten-Sammlers.
          </p>
          <p>
            Dataland unterrichtet den Daten-Sammler, wenn Unternehmens-Daten nicht zugelassen werden. Dataland ist nicht
            verpflichtet, die Gründe für die verweigerte Zulassung anzugeben. Es besteht kein Vergütungsanspruch für
            nicht zugelassene oder später wieder entfernte Unternehmens-Daten.
          </p>
          <p>
            Auch nach Zulassung der Unternehmens-Daten auf die Plattform-Dataland hat Dataland jederzeit und
            uneingeschränkt das Recht, die Unternehmens-Daten nach eigenen und im ausschließlich eigenen Ermessen von
            Dataland liegenden Maßgaben zu prüfen und über deren Verbleib auf der Plattform-Dataland zu entscheiden.
            Insofern kann Dataland jederzeit und frei entscheiden, ob diese vom Daten-Sammler gelieferten
            Unternehmens-Daten als Unternehmens-Daten auf der Plattform-Dataland für den Zugriff von Daten-Usern
            belassen werden oder ob der Zugriff auf diese gesperrt wird. Im Falle einer Sperrung oder Entfernung der
            Unternehmens-Daten gilt § 4.1.6 entsprechend.
          </p>
          <p>
            Der Daten-Sammler hat keinen Anspruch darauf, zu bestimmten Zeiten auf die Plattform-Dataland zuzugreifen
            und zu bestimmten Zeiten Unternehmens-Daten der Plattform-Dataland zu übermitteln. Insbesondere ist Dataland
            nicht verpflichtet, dem Daten-Sammler etwaige Zeiten der Nichtverfügbarkeit der Plattform-Dataland
            mitzuteilen oder in sonstiger Weise eine unbeschränkte und ununterbrochene Verfügbarkeit der
            Plattform-Dataland zu gewährleisten.
          </p>
          <p>
            Dem Daten-Sammler ist bekannt, dass Dataland berechtigt ist, während der Ramp-Up-Phase von Dataland bis zum
            Frühjahr 2024 die Unternehmens-Daten den Daten-Usern und Daten-Ownern kostenfrei zur Verfügung zu stellen
            und dass sich dies unmittelbar auf den Vergütungsanspruch des Daten-Sammlers nach Ziffer 4.4 auswirkt.
          </p>
        </div>
        <div>
          <h3>Einräumung der Nutzungsrechte an Dataland seitens des Daten-Sammlers</h3>
          <p>
            Mit Absenden der Unternehmens-Daten auf der Plattform-Dataland überträgt der Daten-Sammler an Dataland ein
            Nutzungsrecht in der Form, dass Dataland an sämtlichen Daten sowie auch an der Gesamtheit der Daten, und
            zwar auch wenn diese eine Datenbank von § 87a Urhebergesetz bilden sollten, ein unwiderrufliches, räumlich
            und inhaltlich unbeschränktes, zeitlich unbefristetes, übertragbares und unterlizenzierbares Nutzungsrecht
            erhält. Dies umfasst insbesondere das nicht entziehbare und nicht einschränkbare Recht, diese
            Unternehmens-Daten in jeder denkbaren Art und Weise, insbesondere auch im Rahmen jedweder unternehmerischen
            Betätigung, zu nutzen und Dritten zur Verfügung zu stellen oder zu überlassen, so dass auch diese Dritten
            über entsprechende Rechte verfügen. Ebenso hat Dataland das uneingeschränkte Recht, einzelne
            Unternehmens-Daten, die Daten in ihrer Gesamtheit sowie auch etwaige Datenbanken insgesamt zu verändern
            und/oder zu verarbeiten.
          </p>
          <p>
            Für den Fall, dass die Regelungen in diesem Vertrag nicht ausreichen, die entsprechenden Rechte in diesem
            Umfang auf Dataland zu übertragen, ist der Daten-Sammler verpflichtet, eine solche Rechteeinräumung durch
            entsprechende gesonderte rechtsgeschäftliche Erklärungen und tatsächliche Handlungen herbeizuführen und zu
            bewirken.
          </p>
          <p>
            Der Daten-Sammler garantiert, nur solche Unternehmens-Daten an Dataland zu übertragen, die frei von Rechten
            Dritter sind und bei denen weder vertragliche noch gesetzliche Vorgaben einer tatsächlichen Übertragung nach
            § 4.1 oder der rechtswirksamen Rechteübertragung nach diesem § 4.2 entgegenstehen.
          </p>
        </div>
        <div>
          <h3>Pflichten des Daten-Sammlers</h3>
          <p>
            Der Daten-Sammler ist verpflichtet, ausschließlich solche Unternehmens-Daten Dataland zu übertragen, von
            denen der Daten-Sammler nach bestem Wissen und Gewissen und unter Anwendung der im Geschäftsverkehr
            erforderlichen Sorgfalt der Überzeugung ist, dass diese Unternehmens-Daten tatsächlichen Umständen und
            Gegebenheiten entsprechen. Die Unternehmens-Daten dürfen insbesondere auch nicht durch Auslassung von
            Umständen oder Gegebenheiten eine sinnentstellende Aussage treffen.
          </p>
          <p>
            Der Daten-Sammler ist verpflichtet, ausschließlich solche Unternehmens-Daten an Dataland zu übertragen, die
            Dataland auch im Hinblick auf die datenschutzrechtlichen Gesetze und rechtlichen Vorgaben uneingeschränkt
            und insbesondere ohne dahingehende Einwilligungen oder Genehmigungen einzuholen oder ohne sich auf
            gesetzliche Erlaubnistatbestände berufen zu müssen gemäß § 3.1 auf der Plattform-Dataland den Daten-Usern
            zur dort beschriebenen Nutzung zur Verfügung stellen kann.
          </p>
        </div>
        <div>
          <h3>Die Vergütungsansprüche des Daten-Sammlers</h3>
          <p>
            Ausgangspunkt der Vergütung des Daten-Sammlers sind die Unternehmens-Daten, die der Daten-Sammler in Bezug
            auf die einzelnen Unternehmens-Thematiken Dataland zur Verfügung gestellt hat und die infolgedessen von
            Dataland in die Plattform-Dataland aufgenommen wurden und die den Daten-Usern zur Verfügung stehen. Auf den
            tatsächlichen Abruf der entsprechenden Informationen durch Daten-User kommt es im Hinblick auf die Vergütung
            des Daten-Sammlers nicht an.
          </p>
          <p>
            Basierend auf diesem Grundsatz errechnet sich die Vergütung des Daten-Sammlers für das jeweils abgelaufene
            Kalenderjahr wie folgt:
          </p>
          <ul>
            <li>
              Dataland bestimmt durch die Geschäftsführung jeweils nach Abschluss eines Kalenderjahrs einen Anteil der
              gem. § 3.2 erzielten (netto) Einnahmen (ohne Berücksichtigung der Vergütungen, die mittels Dataland-Token
              geleistet wurden), der als Gesamtbetrag zur Vergütung der Daten-Sammler zur Verfügung steht. Für diesen
              Anteil besteht eine untere Grenze welche in der
              <a href="/pricing">aktuellen Preis- und Vergütungsstaffel</a> einsehbar ist.
            </li>
            <li>
              Weiterhin bestimmt Dataland basierend auf den tatsächlich generierten Einnahmen für jeden Kalendertag
              dieses abgelaufenen Kalenderjahres („Claimstage“) den für den jeweiligen Tag zur Verfügung stehenden
              Ausschüttungsbetrag.
            </li>
            <li>
              Schließlich bestimmt Dataland, wie viele Datenpunkte gem. § 4.1.2 an jedem einzelnen Tag insgesamt
              bestanden haben und wie viele Datenpunkte jedem einzelnen Daten-Sammler zuzuordnen sind. Bei Datenpunkten,
              bei denen es Fassungen von unterschiedlicher Aktualität gibt, wird nur die neuste Fassung des Datenpunkts
              in Ansatz gebracht. Dem Daten-Sammler steht der auf ihn entfallende prozentual ermittelte Anteil zu.
            </li>
            <li>
              Vergütungsansprüche unterhalb der technischen Mindestsumme werden nicht ausgezahlt. Die aktuell geltende
              technische Mindestsumme wird in der
              <a href="/pricing">aktuellen Preis- und Vergütungsstaffel</a> angegeben.
            </li>
          </ul>
          <p>
            Auch nach Ende der Registrierung als Daten-Sammler hat der Daten-Sammler für die Unternehmens-Daten, die
            weiterhin auf der Plattform-Dataland verfügbar sind, einen Anspruch auf Vergütung gemäß den vorstehenden
            Bestimmungen.
          </p>
          <p>
            Sämtliche Berechnungsgrundlagen, die nach Vorstehendem der Errechnung des Vergütungsanspruchs des
            Daten-Sammlers zugrunde liegen, werden dem Daten-Sammler zugänglich gemacht, so dass dieser die Berechnung
            seines Vergütungsanspruchs nachvollziehen und verifizieren kann.
          </p>
          <p>
            Die Auszahlung der Vergütung an den Daten-Sammler erfolgt seitens Dataland binnen drei Monaten nach Ende
            eines Kalenderjahres für die vergütungspflichtigen Leistungen des Daten-Sammlers während dieses
            Kalenderjahres.
          </p>
        </div>
        <div>
          <h3>Laufzeit der Registrierung als Daten-Sammler</h3>
          <p>Die Registrierung als Daten-Sammler erfolgt für eine unbestimmte Laufzeit.</p>
          <p>
            Die Registrierung kann von jeder Partei mit einer Frist von einem Monat zum Ende eines Kalendermonats
            gekündigt werden.
          </p>
          <p>Das Recht zur Kündigung aus wichtigem Grund bleibt in jedem Fall unberührt.</p>
        </div>
      </div>
      <div>
        <h2>Für Daten-Owner gelten die folgenden zusätzlichen Bedingungen:</h2>
        <p>
          Daten-Owner können sich ebenfalls als solche auf Dataland registrieren. Als Daten-Owner haben diese das Recht,
          die Plattform-Dataland gleich einem Daten-Sammler oder einem Daten-User zu nutzen.
        </p>
        <p>
          Soweit der Daten-Owner entsprechend einem Daten-User handelt, gelten die Regelungen von § 3 einschließlich
          dortiger Vergütungsregeln.
        </p>
        <p>
          Soweit der Daten-Owner entsprechend einem Daten-Sammler handelt, gelten die Regelungen von § 4 einschließlich
          dortiger Vergütungsregeln.
        </p>
        <div>
          <h3>Vergütungen und Zahlungen</h3>
          <p>
            Die Registrierung als Daten-Owner und die damit verbundene Nutzung der Plattform-Dataland als Daten-Owner
            sind kostenlos, unabhängig davon ob es sich um eine Privatnutzung oder um eine gewerbliche Nutzung handelt.
          </p>
          <p>
            Unabhängig von der Vergütung, die einem Daten-Owner wegen einer Tätigkeit als Daten-Sammler nach § 5.3
            i.V.m. § 4 zusteht, kann dieser eine (zusätzliche) Vergütung nach nachfolgenden Grundsätzen erhalten:
          </p>
          <p>
            Ausgangspunkt der möglichen zusätzlichen Vergütung des Daten-Owners sind die Datenpunkte, die sich während
            eines Kalenderjahres auf den jeweiligen Daten-Owner beziehen.
          </p>
          <p>
            Basierend auf diesem Grundsatz leitet sich eine mögliche zusätzliche Vergütung des Daten-Owners nach Ablauf
            des Kalenderjahres für dieses Kalenderjahr wie folgt her:
          </p>
          <ul>
            <li>
              Dataland kann jeweils nach Abschluss eines Kalenderjahrs einen Anteil der gem. § 3.2 erzielten (netto)
              Einnahmen (ohne Berücksichtigung der Vergütungen, die mittels Dataland-Token geleistet wurden) bestimmen,
              der als Gesamtbetrag zur gesonderten Vergütung der Daten-Owner zur Verfügung stehen soll.
            </li>
            <li>
              Weiterhin bestimmt Dataland basierend auf den tatsächlich generierten Einnahmen für jeden Kalendertag
              dieses abgelaufenen Kalenderjahres („Claimstage“) den für den jeweiligen Tag zur Verfügung stehenden
              Ausschüttungsbetrag.
            </li>
            <li>
              Schließlich bestimmt Dataland, wie viele Datenpunkte gem. § 4.1.2 an jedem einzelnen Tag insgesamt
              bestanden haben und wie viele Datenpunkte jedem einzelnen Daten-Owner zuzuordnen sind. Bei Datenpunkten,
              bei denen es Fassungen von unterschiedlicher Aktualität gibt, wird nur die neuste Fassung des Datenpunkts
              in Ansatz gebracht. Einem Company-Owner steht der auf ihn entfallende prozentual ermittelte Anteil zu
            </li>
            <li>
              Vergütungsansprüche unterhalb der technischen Mindestsumme werden nicht ausgezahlt. Die aktuell geltende
              technische Mindestsumme wird in der
              <a href="/pricing">aktuellen Preis- und Vergütungsstaffel</a> angegeben.
            </li>
          </ul>
          <p>
            Sämtliche Berechnungsgrundlagen, die nach vorstehendem Absatz der Errechnung des Vergütungsanspruchs des
            Company-Owner zugrunde liegen, werden dem Company-Owner zugänglich gemacht, so dass dieser die Berechnung
            seines Vergütungsanspruchs nachvollziehen und verifizieren kann.
          </p>
          <p>
            Die Auszahlung der Vergütung an den Company-Owner erfolgt seitens Dataland binnen drei Monaten nach Ende
            eines Kalenderjahres für die vergütungspflichtigen Leistungen des Daten-Owners während dieses
            Kalenderjahres.
          </p>
        </div>
        <div>
          <h3>Laufzeit der Registrierung als Daten-Owner</h3>
          <p>Die Registrierung als Daten-Owner erfolgt für eine unbestimmte Laufzeit.</p>
          <p>
            Die Registrierung kann von jeder Partei mit einer Frist von einem Monat zum Ende eines Kalendermonats
            gekündigt werden.
          </p>
          <p>Eine Kündigung aus wichtigem Grund bleibt in jedem Fall unberührt.</p>
        </div>
      </div>
      <div>
        <h2>Dataland-Token</h2>
        <p>
          Neben der uneingeschränkt bestehenden Möglichkeit, sämtliche Zahlungsverpflichtungen im Zusammenhang mit der
          Plattform-Dataland durch Zahlungen auf Eurobasis zu erfüllen, besteht mittels der von Dataland geschaffenen
          und herausgegebenen Dataland-Token die Möglichkeit, Zahlungspflichten auch unter Einsatz dieser Dataland-Token
          zu erfüllen. Die Herausgabe und der Einsatz der Dataland-Token erfolgt nach folgenden Grundsätzen:
        </p>
        <p>Dataland wird Dataland-Token schaffen. Jeder Dataland-Token hat einen Nennwert von einem Euro.</p>
        <p>
          Voraussetzung für den Besitz und die Nutzung von Dataland-Token durch Dataland-Teilnehmer ist der Abschluss
          eines <a href="/token">Token-Verwahrvertrags</a> zwischen dem Dataland-Teilnehmer und dem Verwahrer.
        </p>
        <p>
          Dataland verpflichtet sich, Dataland-Token nach den Maßgaben in diesen AGB-Dataland als Zahlungsmittel zu
          akzeptieren, und zwar in der Weise, dass ein Dataland-Token einem Euro entspricht.
        </p>
        <p>
          Dataland ist nicht verpflichtet, Dataland-Token zurück zu nehmen oder für Dataland-Token geleistete Zahlungen
          zurück zu erstatten.
        </p>
        <p>
          Dataland ist in Bezug auf den Einsatz und die Übertragung von Dataland-Token frei und in keiner Weise
          beschränkt. Insbesondere stehen Dataland-Teilnehmern keine Rechte zu, von Dataland in Bezug auf die
          Dataland-Token ein bestimmtes Verhalten verlangen zu können, es sei denn Dataland hat sich kraft
          ausdrücklicher vertraglicher Regelungen in Bezug auf die Dataland-Token zu einem bestimmten Verhalten
          verpflichtet.
        </p>
        <div>
          <h3>
            Dataland beabsichtigt, die Dataland-Token wie folgt den Dataland-Teilnehmern anzubieten und an diese zu
            übertragen:
          </h3>
          <p>
            Dataland beabsichtigt Company-Ownern den Ankauf von Dataland-Token anzubieten. Hierbei handelt Dataland
            weder im Hinblick auf die Anzahl der jeweils angebotenen Dataland-Token noch im Hinblick auf deren
            Bepreisung in freiem Ermessen und insbesondere ohne die Verpflichtung, die Company-Owner gleich behandeln zu
            müssen.
          </p>
          <p>
            Dataland beabsichtigt Daten-Sammlern die Erfüllung ihrer Vergütungsansprüche mit Dataland-Token anzubieten.
            Hierbei handelt Dataland sowohl im Hinblick auf die Anzahl der jeweils angebotenen Dataland-Token als auch
            im Hinblick auf deren Bepreisung in freiem Ermessen und insbesondere ohne die Verpflichtung, die
            Data-Sammler gleich behandeln zu müssen. Daten-Sammler sind frei in ihrer Entscheidung, ob sie ein
            entsprechendes Angebot annehmen wollen.
          </p>
          <p>
            Dataland kann auch den übrigen Dataland-Teilnehmern den Ankauf von Dataland-Token anbieten. Auch hierbei
            handelt Dataland sowohl im Hinblick auf die Anzahl der jeweils angebotenen Dataland-Token als auch im
            Hinblick auf deren Bepreisung in freiem Ermessen und insbesondere ohne die Verpflichtung, dahingehend die
            Dataland-Teilnehmer gleich behandeln zu müssen.
          </p>
        </div>
        <p>
          Sobald Dataland Token auf Dataland nutzbar und käuflich sind, haben Dataland-Teilnehmer das Recht, ihre
          jeweiligen Zahlungspflichten gegenüber Dataland zu 50% mit Dataland-Token zu erfüllen.
        </p>
      </div>
      <div>
        <h2>Datenschutz</h2>
        <p>
          Dataland erhebt, verarbeitet und nutzt personenbezogene Daten der Dataland-Teilnehmer unter strenger
          Einhaltung der geltenden Gesetze, insbesondere der DS-GVO und des Bundesdatenschutzgesetztes. Nähere
          Informationen enthält unsere <a href="/dataprivacy">Datenschutzerklärung</a>.
        </p>
      </div>
      <div>
        <h2>Inhalt und Änderungen der AGB-Dataland</h2>
        <p>
          Änderungen oder Ergänzungen dieser AGB-Dataland sowie sonstige Vereinbarungen zwischen Dataland und dem
          Nutzer, die den Regelungen der AGB-Dataland unterstellt sind, bedürfen der Schriftform.
        </p>
        <p>
          Sollte eine Bestimmung dieser AGB-Dataland unwirksam sein oder werden, so soll hierdurch die Wirksamkeit der
          AGB-Dataland im Übrigen nicht berührt werden. Dataland und der Nutzer verpflichten sich bereits jetzt, eine
          neue wirksame Regelung zu vereinbaren, die dem Zweck der unwirksamen Regelung möglichst nahekommt.
          Entsprechendes gilt bei Vertragslücken.
        </p>
        <p>
          Dataland kann diese AGB-Dataland dadurch aktualisieren oder ändern, dass Dataland dem Dataland-Teilnehmer die
          geänderten Vertragsdokumente zur Annahme anbietet und ihn zur Annahme auffordert.
        </p>
        <p>
          Nimmt der Dataland-Teilnehmer die geänderten Vertragsunterlagen nicht binnen 8 Wochen an, hat Dataland das
          Recht, sämtliche auf den Dataland-Teilnehmer laufende Registrierungen mit sofortiger Wirkung durch Kündigung
          zu beenden.
        </p>
      </div>
      <div>
        <h2>Schlussbestimmungen</h2>
        <p>
          Der registrierte Nutzer kann Rechte, Ansprüche und Rechtspositionen aus diesem Vertrag nur mit schriftlicher
          Zustimmung von Dataland an Dritte übertragen bzw. an diese abgetreten.
        </p>
        <p>
          Die durch diese AGB-Dataland geregelte Geschäftsbeziehung zwischen Dataland und dem Nutzer unterliegt dem
          Recht der Bundesrepublik Deutschland, unter Ausschluss des UN-Kaufrechts („C.I.S.G“).
        </p>
        <p>
          Sofern der Dataland Nutzer Kaufmann ist oder keinen allgemeinen Gerichtsstand im Inland hat ist Erfüllungsort
          und ausschließlicher Gerichtsstand für alle Rechtsstreitigkeiten, die sich aus oder im Zusammenhang mit der
          durch diese AGB-Dataland geregelten Geschäftsbeziehung zwischen Dataland und dem Dataland Nutzer, ergeben,
          Frankfurt am Main (Deutschland).
        </p>
      </div>
      <div>
        <h2>Vorrang der deutschen Fassung</h2>
        <p>
          Maßgeblich ist die deutsche Fassung dieser AGB-Dataland. Die englische Fassung dient ausschließlich
          informatorischen Zwecken zugunsten nicht deutsch sprechender Personen.
        </p>
      </div>
      <div class="w-12 text-center font-bold mt-6">- Ende der AGB-Dataland -</div>
    </div>
  </LegalPage>
</template>

<script lang="ts">
import LegalPage from '@/components/generics/LegalPage.vue';
import { computed, defineComponent } from 'vue';
import InputSwitch from 'primevue/inputswitch';

export default defineComponent({
  name: 'TermsAndConditions',
  components: { LegalPage, InputSwitch },
  data() {
    return {
      languageGerman: false,
    };
  },
  provide() {
    return {
      hideEmptyFields: computed(() => {
        return this.languageGerman;
      }),
    };
  },
});
</script>

<style scoped>
.no-numbering {
  counter-reset: section subsection;
}

div.no-numbering h2::before,
div.no-numbering h2 ~ p::before {
  counter-increment: none;
  content: '';
}

h1 {
  counter-reset: section;
}

h2 {
  counter-reset: subsection;
}

:not(.no-numbering) h2::before {
  counter-increment: section;
  content: counter(section) '. ';
}

:not(.no-numbering) h2 ~ p::before {
  counter-increment: subsection;
  content: counter(section) '.' counter(subsection) '. ';
}

h3::before {
  counter-increment: subsection;
  content: counter(section) '.' counter(subsection) '. ';
}

h3 {
  counter-reset: subsubsection;
}

p ~ div > h3,
p {
  font-size: 1rem;
  font-weight: normal;
  ::before {
    font-weight: bold;
  }
}

p ~ div > h3::before,
p::before {
  font-weight: bold;
}

h3 ~ p:not(:only-of-type)::before {
  counter-increment: subsubsection;
  content: counter(section) '.' counter(subsection) '.' counter(subsubsection) '. ';
}
</style>
