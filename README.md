# UniJobs (UnicvJobs)

Aplicativo Android desenvolvido como projeto da disciplina de **Aplicações Android** na **Universidade de Cabo Verde (Uni-CV)**.

O UniJobs conecta **estudantes universitários** a **empresas**, funcionando como uma plataforma simplificada de vagas de emprego/estágio: empresas publicam oportunidades e estudantes se candidatam anexando o currículo em PDF.

## 📱 Screenshots

<!-- Adicione aqui os prints do app, exemplo: -->
<!-- <img src="screenshots/tela_login.png" width="250"/> <img src="screenshots/lista_vagas.png" width="250"/> -->

## ✨ Funcionalidades

- **Autenticação** de usuários com dois perfis: Aluno (estudante) e Empresa
- **Empresas**: criar e publicar vagas, visualizar candidatos, aprovar/reprovar candidaturas, encerrar vagas
- **Alunos**: navegar pelas vagas disponíveis, candidatar-se anexando currículo em **PDF**, acompanhar status das candidaturas (Pendente / Aprovado / Reprovado)
- Upload e visualização de currículos em PDF via Cloudinary
- Perfis editáveis para aluno e empresa

## 🛠️ Tecnologias

- **Kotlin** (Android nativo)
- **Firebase Authentication** — autenticação de usuários
- **Firebase Firestore** — banco de dados em tempo real
- **Cloudinary** — armazenamento dos currículos em PDF (upload unsigned via API REST)
- **OkHttp** — requisições HTTP para upload dos PDFs
- **Material Design 3** (Material Components for Android)
- **View Binding**

## 🚀 Como rodar o projeto

1. Clone o repositório:
   ```bash
   git clone https://github.com/ailton2831/UniJobs.git
   ```
2. Abra o projeto no **Android Studio**.
3. Crie um projeto no [Firebase Console](https://console.firebase.google.com) e ative:
   - Authentication (método Email/Senha)
   - Firestore Database
4. Baixe o arquivo `google-services.json` do seu projeto Firebase e coloque em `app/google-services.json` (esse arquivo não é versionado no repositório por segurança).
5. Configure as **Regras de Segurança** do Firestore para exigir autenticação (ver seção abaixo).
6. Crie uma conta gratuita no [Cloudinary](https://cloudinary.com) e configure:
   - Um **Upload Preset** do tipo **Unsigned** (Settings → Upload → Upload presets)
   - Anote o **Cloud Name** (aparece no topo do Dashboard) e o nome do preset criado
   - Insira esses dois valores em `Candidato.kt` (constantes `CLOUD_NAME` e `UPLOAD_PRESET`)
7. Sincronize o Gradle e rode o app em um emulador ou dispositivo físico.

### Regras de segurança recomendadas (Firestore)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### Sobre o armazenamento dos currículos (Cloudinary)

Os PDFs de currículo são enviados via upload **unsigned** para o Cloudinary (plano gratuito, sem necessidade de cartão de crédito). A URL pública retornada é salva no campo `cvUrl` da candidatura, no Firestore.

**Nota de segurança:** no plano gratuito do Cloudinary, a URL do PDF fica pública (não-listada) — qualquer pessoa com o link exato consegue acessar, mesmo sem estar autenticada no app, de forma similar a um link de compartilhamento do Google Drive. Restringir esse acesso exigiria URLs assinadas com expiração, recurso disponível apenas em planos pagos.

## 📂 Estrutura do projeto

```
com.example.unicvjobs
├── admin/          # Dashboard e listagens administrativas
├── autenticacao/    # Login, registo e tela de autenticação
├── classes/         # Data classes (models): Vaga, Candidatura, User, Empresa
├── empresa/         # Telas do perfil empresa: criar vaga, perfil, setup
├── estudante/        # Telas do perfil aluno: candidaturas, perfil, setup
├── vaga/             # Detalhe de vaga
├── adapter/          # Adapters de RecyclerView (Vagas, Candidaturas)
└── splash/           # Tela inicial de carregamento
```

## 👥 Autores

- **Ailton Leniny**
- **Robson Afonso**
- **Nuno Fernandes**

Projeto acadêmico — Universidade de Cabo Verde (Uni-CV), Curso de Engenharia Informática.

## 📄 Licença

Projeto acadêmico sem fins comerciais.
